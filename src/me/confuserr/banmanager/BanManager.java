package me.confuserr.banmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.confuserr.banmanager.Database;
import me.confuserr.banmanager.Commands.*;
import me.confuserr.banmanager.listeners.*;
import me.confuserr.banmanager.scheduler.*;
import net.h31ix.updater.Updater;

import org.bukkit.plugin.java.JavaPlugin;

public class BanManager extends JavaPlugin {
	public Logger logger = Logger.getLogger("Minecraft");
	public BanManager plugin;
	public static BanManager staticPlugin;
	public Database localConn;
	public String serverName;

	public Map<String, String> banMessages = new HashMap<String, String>();
	public boolean logKicks;
	public List<String> toUnbanPlayer = Collections.synchronizedList(new ArrayList<String>());
	public List<String> toUnbanIp = Collections.synchronizedList(new ArrayList<String>());

	public int keepKicks, keepBanRecords, keepIPBanRecords, keepIPs,
			keepMuteRecords, keepWarnings;

	public boolean checkForUpdates = true;
	public boolean updateAvailable = false;
	public HashSet<String> mutedBlacklist = new HashSet<String>();
	public HashMap<String, String> timeLimitsMutes = new HashMap<String, String>();
	public HashMap<String, String> timeLimitsBans = new HashMap<String, String>();

	public DbLogger dbLogger;
	public String updateVersion;

	public ConcurrentHashMap<String, Long> mutedPlayersLength = new ConcurrentHashMap<String, Long>();
	public ConcurrentHashMap<String, String> mutedPlayersReason = new ConcurrentHashMap<String, String>();
	public ConcurrentHashMap<String, String> mutedPlayersBy = new ConcurrentHashMap<String, String>();

	public List<String> bannedPlayers = Collections.synchronizedList(new ArrayList<String>());
	public List<String> bannedIps = Collections.synchronizedList(new ArrayList<String>());

	public boolean usePartialNames = true;
	public boolean bukkitBan = true;
	public boolean logIPs;

	@Override
	public void onDisable() {
		// Cancel all BanManager tasks
		getServer().getScheduler().cancelTasks(this);

		// Check to see if any bukkit unbans must take place
		if (plugin.toUnbanPlayer.size() > 0 || plugin.toUnbanIp.size() > 0) {
			new bukkitUnbanSync(plugin).run();
		}

		// Close the database connection
		localConn.close();

		logger.info("[BanManager] has been disabled");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		plugin = this;
		staticPlugin = this;

		// Load config
		configReload();

		// Initilise database
		localConn = new Database(getConfig().getString("localDatabase.username"), getConfig().getString("localDatabase.password"), "jdbc:mysql://" + getConfig().getString("localDatabase.host") + ":" + getConfig().getString("localDatabase.port") + "/" + getConfig().getString("localDatabase.database") + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10" + (getConfig().getBoolean("useUTF8") ? "&useUnicode=true&characterEncoding=utf-8" : ""), this);

		plugin.dbLogger = new DbLogger(localConn, plugin);

		if (!localConn.checkConnection()) {
			this.logger.severe("[BanManager] is unable to connect to the database, it has been disabled");
			plugin.getPluginLoader().disablePlugin(this);
			return;
		}

		if (!localConn.checkTable(localConn.warningsTable)) {
			// Modify the bminfo message for warnings
			getConfig().set("messages.bmInfo", getConfig().getString("messages.bmInfo") + "\n&cWarnings: [warningsCount]");
			saveConfig();

			this.logger.info("[BanManager] creating tables");
			try {
				plugin.dbLogger.create_tables();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			// Convert player names to lowercase
			localConn.query("UPDATE " + localConn.bansTable + " SET banned = LOWER(banned)");
		}

		getCommand("ban").setExecutor(new BanCommand(this));
		getCommand("tempban").setExecutor(new TempBanCommand(this));
		getCommand("unban").setExecutor(new UnBanCommand(this));
		getCommand("bminfo").setExecutor(new BmInfoCommand(this));
		getCommand("banip").setExecutor(new BanIpCommand(this));
		getCommand("tempbanip").setExecutor(new TempBanIpCommand(this));
		getCommand("unbanip").setExecutor(new UnBanIpCommand(this));
		getCommand("banimport").setExecutor(new BanImportCommand(this));
		getCommand("kick").setExecutor(new KickCommand(this));
		getCommand("nlkick").setExecutor(new LoglessKickCommand(this));
		getCommand("mute").setExecutor(new MuteCommand(this));
		getCommand("tempmute").setExecutor(new TempMuteCommand(this));
		getCommand("unmute").setExecutor(new UnMuteCommand(this));
		getCommand("bmreload").setExecutor(new ReloadCommand(this));
		getCommand("bmtools").setExecutor(new BmToolsCommand(this));
		getCommand("warn").setExecutor(new WarnCommand(this));

		if (getConfig().getBoolean("useSyncChat")) { // If syncChat is on, use
														// Sync events
			getServer().getPluginManager().registerEvents(new SyncLogin(plugin), this);
			getServer().getPluginManager().registerEvents(new SyncChat(plugin), this);
		} else if (getServer().getOnlineMode()) { // If server is in online mode
													// and syncChat is off, use
													// async events
			getServer().getPluginManager().registerEvents(new AsyncPreLogin(plugin), this);
			getServer().getPluginManager().registerEvents(new AsyncChat(plugin), this);
		} else { // Otherwise use the normal sync login event and use Async chat
					// even for mutes.
			getServer().getPluginManager().registerEvents(new SyncLogin(plugin), this);
			getServer().getPluginManager().registerEvents(new AsyncChat(plugin), this);
		}

		// Register the blacklist check event for mutes
		getServer().getPluginManager().registerEvents(new MutedBlacklistCheck(plugin), this);

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

		logger.info("[BanManager] Version:" + getDescription().getVersion() + " has been enabled");

		// Checks for expired bans, and moves them into the record table
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new databaseAsync(this), 2400L, getConfig().getInt("scheduler.expiresCheck", 300) * 20);
		// 2 minute delay before it starts, runs every 5 minutes

		// Bukkit unban bans those that have expired
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new bukkitUnbanSync(this), 10L, getConfig().getInt("scheduler.bukkitUnban", 3) * 20);

		// Check the muted table for new mutes
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new muteAsync(this), 20L, getConfig().getInt("scheduler.newMutes", 8) * 20);

		// Check the banned tables for new player bans
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new bansAsync(this), 21L, getConfig().getInt("scheduler.newBans", 8) * 20);

		// Check the ip table for new ip bans
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ipBansAsync(this), 22L, getConfig().getInt("scheduler.newIPBans", 8) * 20);

		// Load all the player & ip bans into the array
		ResultSet result = localConn.query("SELECT * FROM " + localConn.bansTable);

		int playerBans = 0;

		try {
			while (result.next()) {
				// Add them to the banned list
				plugin.bannedPlayers.add(result.getString("banned").toLowerCase());
				playerBans++;
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		logger.info("[BanManager] " + "Loaded " + playerBans + " player bans");

		ResultSet result1 = localConn.query("SELECT banned FROM " + localConn.ipBansTable);

		int ipBans = 0;

		try {
			while (result1.next()) {
				// Add them to the banned list
				plugin.bannedIps.add(result1.getString("banned"));
				ipBans++;
			}

			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		logger.info("[BanManager] " + "Loaded " + ipBans + " ip bans");

		// Check for an update
		if (checkForUpdates) {
			Updater updater = new Updater(this, "ban-management", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			// Determine if there is an update ready for us
			updateAvailable = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;

			if (updateAvailable) {
				// Get the latest version
				updateVersion = updater.getLatestVersionString();

				logger.info("[BanManager] " + updateVersion + " update available");
				getServer().getPluginManager().registerEvents(new UpdateNotify(plugin), this);
			}
		}
	}

	// Reloads everything in the config except the database details
	public void configReload() {
		logKicks = getConfig().getBoolean("logKicks");
		keepKicks = getConfig().getInt("cleanUp.keepKicks");

		logIPs = getConfig().getBoolean("logIPs");
		keepIPs = getConfig().getInt("cleanUp.playerIPs");

		keepBanRecords = getConfig().getInt("cleanUp.banRecords");
		keepIPBanRecords = getConfig().getInt("cleanUp.ipBanRecords");
		keepMuteRecords = getConfig().getInt("cleanUp.muteRecords");
		keepWarnings = getConfig().getInt("cleanUp.warnings");

		serverName = getConfig().getString("serverName");

		checkForUpdates = getConfig().getBoolean("checkForUpdates");

		usePartialNames = getConfig().getBoolean("use-partial-names");

		bukkitBan = getConfig().getBoolean("bukkit-ban");

		banMessages.clear();

		for (String key : getConfig().getConfigurationSection("messages").getKeys(false)) {
			banMessages.put(key, Util.colorize(getConfig().getString("messages." + key).replace("\\n", "\n")));
		}

		for (String cmd : getConfig().getStringList("mutedCommandBlacklist")) {
			mutedBlacklist.add(cmd);
		}

		timeLimitsMutes.clear();
		// Loop through the time limits
		for (String key : getConfig().getConfigurationSection("timeLimits.mutes").getKeys(false)) {
			String path = "timeLimits.mutes." + key;
			timeLimitsMutes.put(key, getConfig().getString(path));
		}

		timeLimitsBans.clear();
		for (String key : getConfig().getConfigurationSection("timeLimits.bans").getKeys(false)) {
			String path = "timeLimits.bans." + key;
			timeLimitsBans.put(key, getConfig().getString(path));
		}
	}

	public final String getIp(InetAddress ip) {
		return ip.getHostAddress().replace("/", "");
	}

	public String getIp(String ip) {
		ip = ip.replace("/", "");

		String[] withoutPort = ip.split(":");

		if (withoutPort.length == 2)
			return withoutPort[0];
		else
			return ip;
	}

	public void addMute(String player, String reason, String by, long length) {
		plugin.mutedPlayersBy.put(player, by);
		plugin.mutedPlayersLength.put(player, length * 1000);
		plugin.mutedPlayersReason.put(player, reason);
	}

	public void removeMute(String player) {
		plugin.dbLogger.muteRemove(player, "Console automated");
		if (plugin.mutedPlayersBy.containsKey(player)) {
			plugin.mutedPlayersBy.remove(player);
			plugin.mutedPlayersLength.remove(player);
			plugin.mutedPlayersReason.remove(player);
		}
	}

	public void removeMute(String player, String by) {
		plugin.dbLogger.muteRemove(player, by);
		if (plugin.mutedPlayersBy.containsKey(player)) {
			plugin.mutedPlayersBy.remove(player);
			plugin.mutedPlayersLength.remove(player);
			plugin.mutedPlayersReason.remove(player);
		}
	}

	public void removeHashMute(String player) {
		if (plugin.mutedPlayersBy.containsKey(player)) {
			plugin.mutedPlayersBy.remove(player);
			plugin.mutedPlayersLength.remove(player);
			plugin.mutedPlayersReason.remove(player);
		}
	}

	public static BanManager getPlugin() {
		return staticPlugin;
	}

	// Copyright essentials, all credits to them, this is here to remove
	// dependency on it, I did not create these functions!
	public long parseDateDiff(String time, boolean future) throws Exception {
		Pattern timePattern = Pattern.compile("(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?" + "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
		Matcher m = timePattern.matcher(time);
		int years = 0;
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;
		boolean found = false;
		while (m.find()) {
			if (m.group() == null || m.group().isEmpty()) {
				continue;
			}
			for (int i = 0; i < m.groupCount(); i++) {
				if (m.group(i) != null && !m.group(i).isEmpty()) {
					found = true;
					break;
				}
			}
			if (found) {
				if (m.group(1) != null && !m.group(1).isEmpty())
					years = Integer.parseInt(m.group(1));
				if (m.group(2) != null && !m.group(2).isEmpty())
					months = Integer.parseInt(m.group(2));
				if (m.group(3) != null && !m.group(3).isEmpty())
					weeks = Integer.parseInt(m.group(3));
				if (m.group(4) != null && !m.group(4).isEmpty())
					days = Integer.parseInt(m.group(4));
				if (m.group(5) != null && !m.group(5).isEmpty())
					hours = Integer.parseInt(m.group(5));
				if (m.group(6) != null && !m.group(6).isEmpty())
					minutes = Integer.parseInt(m.group(6));
				if (m.group(7) != null && !m.group(7).isEmpty())
					seconds = Integer.parseInt(m.group(7));
				break;
			}
		}
		if (!found)
			throw new Exception("Illegal Date");
		Calendar c = new GregorianCalendar();
		if (years > 0)
			c.add(Calendar.YEAR, years * (future ? 1 : -1));
		if (months > 0)
			c.add(Calendar.MONTH, months * (future ? 1 : -1));
		if (weeks > 0)
			c.add(Calendar.WEEK_OF_YEAR, weeks * (future ? 1 : -1));
		if (days > 0)
			c.add(Calendar.DAY_OF_MONTH, days * (future ? 1 : -1));
		if (hours > 0)
			c.add(Calendar.HOUR_OF_DAY, hours * (future ? 1 : -1));
		if (minutes > 0)
			c.add(Calendar.MINUTE, minutes * (future ? 1 : -1));
		if (seconds > 0)
			c.add(Calendar.SECOND, seconds * (future ? 1 : -1));
		return c.getTimeInMillis();
	}

	public String formatDateDiff(Calendar fromDate, Calendar toDate) {
		boolean future = false;
		if (toDate.equals(fromDate)) {
			return banMessages.get("timeNow");
		}
		if (toDate.after(fromDate)) {
			future = true;
		}

		StringBuilder sb = new StringBuilder();
		int[] types = new int[] { Calendar.YEAR, Calendar.MONTH, Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND };
		String[] names = new String[] { banMessages.get("timeYear"), banMessages.get("timeYears"), banMessages.get("timeMonth"), banMessages.get("timeMonths"), banMessages.get("timeDay"), banMessages.get("timeDays"), banMessages.get("timeHour"), banMessages.get("timeHours"), banMessages.get("timeMinute"), banMessages.get("timeMinutes"), banMessages.get("timeSecond"), banMessages.get("timeSeconds") };
		for (int i = 0; i < types.length; i++) {
			int diff = dateDiff(types[i], fromDate, toDate, future);
			if (diff > 0) {
				sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
			}
		}
		if (sb.length() == 0) {
			return "now";
		}
		return sb.toString().trim();
	}

	public String formatDateDiff(long date) {
		Calendar now = new GregorianCalendar();
		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(date);
		return formatDateDiff(now, c);
	}

	private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future) {
		int diff = 0;
		long savedDate = fromDate.getTimeInMillis();
		while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate))) {
			savedDate = fromDate.getTimeInMillis();
			fromDate.add(type, future ? 1 : -1);
			diff++;
		}
		diff--;
		fromDate.setTimeInMillis(savedDate);
		return diff;
	}
}
