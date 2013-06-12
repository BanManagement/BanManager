package me.confuserr.banmanager;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.confuserr.banmanager.Database;
import me.confuserr.banmanager.Commands.*;
import me.confuserr.banmanager.Listeners.*;
import me.confuserr.banmanager.Scheduler.*;
import me.confuserr.banmanager.data.*;
import net.h31ix.updater.Updater;

import org.bukkit.plugin.java.JavaPlugin;

public class BanManager extends JavaPlugin {
	public BanManager plugin;
	public static BanManager staticPlugin;
	public Database localConn;
	public String serverName;

	private Map<String, String> banMessages = new HashMap<String, String>();
	public boolean logKicks;

	public enum CleanUp {
		Kicks(30), PlayerIPs(0), Warnings(0), BanRecords(0), IPBanRecords(0), MuteRecords(0);

		private int days = 0;
		private long millis = 0;

		private CleanUp(int length) {
			days = length;
			millis = days * 86400;
		}

		public int getDays() {
			return days;
		}

		public void setDays(int length) {
			days = length;
			millis = days * 86400;
		}

		public long getDaysInMilliseconds() {
			return millis;
		}
	}

	private boolean checkForUpdates = true;
	private boolean updateAvailable = false;
	private HashSet<String> mutedBlacklist = new HashSet<String>();
	private HashMap<String, String> timeLimitsMutes = new HashMap<String, String>();
	private HashMap<String, String> timeLimitsBans = new HashMap<String, String>();

	public DbLogger dbLogger;
	public String updateVersion;
	public File jarFile;

	private ConcurrentHashMap<String, MuteData> playerMutes = new ConcurrentHashMap<String, MuteData>();
	private ConcurrentHashMap<String, BanData> playerBans = new ConcurrentHashMap<String, BanData>();
	private ConcurrentHashMap<String, IPBanData> ipBans = new ConcurrentHashMap<String, IPBanData>();

	private boolean usePartialNames = true;
	private boolean bukkitBans = true;
	private boolean logIPs = true;

	@Override
	public void onDisable() {
		// Cancel all BanManager tasks
		getServer().getScheduler().cancelTasks(this);

		// Close the database connection
		localConn.close();

		getLogger().info("[BanManager] has been disabled");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveConfig();
		plugin = this;
		staticPlugin = this;

		// Load config
		configReload();

		// Initialise database
		localConn = new Database(getConfig().getString("localDatabase.username"), getConfig().getString("localDatabase.password"), "jdbc:mysql://" + getConfig().getString("localDatabase.host") + ":" + getConfig().getString("localDatabase.port") + "/" + getConfig().getString("localDatabase.database") + "?autoReconnect=true&failOverReadOnly=false&maxReconnects=10" + (getConfig().getBoolean("useUTF8") ? "&useUnicode=true&characterEncoding=utf-8" : ""), this);

		plugin.dbLogger = new DbLogger(localConn, plugin);

		if (!localConn.checkConnection()) {
			getLogger().severe("Unable to connect to the database, it has been disabled");
			plugin.getPluginLoader().disablePlugin(this);
			return;
		}

		if (!localConn.checkTable(localConn.warningsTable)) {
			// Modify the bminfo message for warnings
			getConfig().set("messages.bmInfo", getConfig().getString("messages.bmInfo") + "\n&cWarnings: [warningsCount]");
			saveConfig();

			getLogger().info("[BanManager] creating tables");
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

		getLogger().info("Version:" + getDescription().getVersion() + " has been enabled");

		// Checks for expired bans, and moves them into the record table
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new databaseAsync(this), 2400L, getConfig().getInt("scheduler.expiresCheck", 300) * 20);
		// 2 minute delay before it starts, runs every 5 minutes

		// Check the muted table for new mutes
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new muteAsync(this), 20L, getConfig().getInt("scheduler.newMutes", 8) * 20);

		// Check the banned tables for new player bans
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new bansAsync(this), 21L, getConfig().getInt("scheduler.newBans", 8) * 20);

		// Check the ip table for new ip bans
		getServer().getScheduler().scheduleAsyncRepeatingTask(this, new ipBansAsync(this), 22L, getConfig().getInt("scheduler.newIPBans", 8) * 20);

		// Load all the player & ip bans into the array
		ResultSet result = localConn.query("SELECT banned, ban_reason, banned_by, ban_time, ban_expires_on FROM " + localConn.bansTable);

		try {
			while (result.next()) {
				// Add them to the banned list
				playerBans.put(result.getString("banned").toLowerCase(), new BanData(result.getString("banned").toLowerCase(), result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expires_on")));
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		getLogger().info("Loaded " + playerBans.size() + " player bans");

		ResultSet result1 = localConn.query("SELECT  banned, ban_reason, banned_by, ban_time, ban_expires_on FROM " + localConn.ipBansTable);

		try {
			while (result1.next()) {
				// Add them to the banned list
				ipBans.put(result.getString("banned"), new IPBanData(result.getString("banned"), result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expires_on")));
			}

			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		getLogger().info("Loaded " + ipBans.size() + " ip bans");

		// Check for an update
		if (checkForUpdates) {
			Updater updater = new Updater(this, "ban-management", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			// Determine if there is an update ready for us
			updateAvailable = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE;

			if (updateAvailable) {
				// Get the latest version
				updateVersion = updater.getLatestVersionString();

				jarFile = getFile();

				getLogger().info("[BanManager] " + updateVersion + " update available");
				getServer().getPluginManager().registerEvents(new UpdateNotify(plugin), this);
			}
		}
	}

	// Reloads everything in the config except the database details
	public void configReload() {
		reloadConfig();

		logKicks = getConfig().getBoolean("logKicks");
		CleanUp.Kicks.setDays(getConfig().getInt("cleanUp.keepKicks"));

		logIPs = getConfig().getBoolean("logIPs");
		CleanUp.PlayerIPs.setDays(getConfig().getInt("cleanUp.playerIPs"));

		CleanUp.BanRecords.setDays(getConfig().getInt("cleanUp.banRecords"));
		CleanUp.IPBanRecords.setDays(getConfig().getInt("cleanUp.ipBanRecords"));
		CleanUp.MuteRecords.setDays(getConfig().getInt("cleanUp.muteRecords"));
		CleanUp.Warnings.setDays(getConfig().getInt("cleanUp.warnings"));

		serverName = getConfig().getString("serverName");

		checkForUpdates = getConfig().getBoolean("checkForUpdates");

		usePartialNames = getConfig().getBoolean("use-partial-names");

		bukkitBans = getConfig().getBoolean("bukkit-ban");

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

		getTimeLimitsBans().clear();
		for (String key : getConfig().getConfigurationSection("timeLimits.bans").getKeys(false)) {
			String path = "timeLimits.bans." + key;
			getTimeLimitsBans().put(key, getConfig().getString(path));
		}
	}

	public ConcurrentHashMap<String, BanData> getPlayerBans() {
		return playerBans;
	}

	public ConcurrentHashMap<String, IPBanData> getIPBans() {
		return ipBans;
	}

	public ConcurrentHashMap<String, MuteData> getPlayerMutes() {
		return playerMutes;
	}

	public static BanManager getPlugin() {
		return staticPlugin;
	}

	public boolean usePartialNames() {
		return usePartialNames;
	}

	public boolean useBukkitBans() {
		return bukkitBans;
	}

	public boolean logIPs() {
		return logIPs;
	}

	public boolean isUpdateAvailable() {
		return updateAvailable;
	}

	public boolean checkForUpdates() {
		return checkForUpdates;
	}

	public String getMessage(String message) {
		return banMessages.get(message);
	}

	public HashMap<String, String> getTimeLimitsBans() {
		return timeLimitsBans;
	}

	public HashMap<String, String> getTimeLimitsMutes() {
		return timeLimitsMutes;
	}

	public HashSet<String> getMutedBlacklist() {
		return mutedBlacklist;
	}

	public void addPlayerBan(String name, String bannedBy, String reason) {
		name = name.toLowerCase();

		playerBans.put(name, new BanData(name, bannedBy, reason, System.currentTimeMillis() / 1000, 0));

		dbLogger.logBan(name, bannedBy, reason);
	}

	public void addPlayerBan(String name, String bannedBy, String reason, long expires) {
		name = name.toLowerCase();

		playerBans.put(name, new BanData(name, bannedBy, reason, System.currentTimeMillis() / 1000, expires));

		dbLogger.logTempBan(name, bannedBy, reason, expires);
	}

	public void addPlayerBan(BanData data) {
		playerBans.put(data.getBanned(), data);
	}

	public void removePlayerBan(String name, String by, boolean keepLog) {
		name = name.toLowerCase();

		playerBans.remove(name);
		dbLogger.banRemove(name, by, keepLog);

		if (useBukkitBans())
			getServer().getOfflinePlayer(name).setBanned(false);
	}

	public BanData getPlayerBan(String name) {
		return playerBans.get(name.toLowerCase());
	}

	public ArrayList<BanData> getPlayerPastBans(String name) {
		return dbLogger.getPastBans(name.toLowerCase());
	}

	public boolean isPlayerBanned(String name) {
		return playerBans.get(name.toLowerCase()) != null;
	}

	public void addIPBan(String ip, String bannedBy, String reason) {
		ipBans.put(ip, new IPBanData(ip, bannedBy, reason, System.currentTimeMillis() / 1000, 0));

		dbLogger.logIpBan(ip, bannedBy, reason);
	}

	public void addIPBan(String ip, String bannedBy, String reason, long expires) {
		ipBans.put(ip, new IPBanData(ip, bannedBy, reason, System.currentTimeMillis() / 1000, expires));

		dbLogger.logTempIpBan(ip, bannedBy, reason, expires);
	}

	public void addIPBan(IPBanData data) {
		ipBans.put(data.getBanned(), data);
	}

	public void removeIPBan(String ip, String by, boolean keepLog) {
		ipBans.remove(ip);
		dbLogger.ipRemove(ip, by, keepLog);

		if (useBukkitBans())
			getServer().unbanIP(ip);
	}

	public IPBanData getIPBan(String ip) {
		return ipBans.get(ip);
	}

	public ArrayList<IPBanData> getIPPastBans(String ip) {
		return dbLogger.getPastIPBans(ip);
	}

	public boolean isIPBanned(String ip) {
		return ipBans.get(ip) != null;
	}

	public void addPlayerMute(String name, String bannedBy, String reason) {
		name = name.toLowerCase();

		playerMutes.put(name, new MuteData(name, bannedBy, reason, System.currentTimeMillis() / 1000, 0));

		dbLogger.logMute(name, bannedBy, reason);
	}

	public void addPlayerMute(String name, String bannedBy, String reason, long expires) {
		name = name.toLowerCase();

		playerMutes.put(name, new MuteData(name, bannedBy, reason, System.currentTimeMillis() / 1000, expires));

		dbLogger.logTempMute(name, bannedBy, reason, expires);
	}

	public void addPlayerMute(MuteData data) {
		playerMutes.put(data.getMuted(), data);
	}

	public void removePlayerMute(String name, String by, boolean keepLog) {
		name = name.toLowerCase();

		playerMutes.remove(name);
		dbLogger.muteRemove(name, by, keepLog);
	}

	public MuteData getPlayerMute(String name) {
		name = name.toLowerCase();

		if (playerMutes.get(name) != null)
			return playerMutes.get(name.toLowerCase());
		
		return dbLogger.getMute(name);
	}

	public ArrayList<MuteData> getPlayerPastMutes(String name) {
		return dbLogger.getPastMutes(name.toLowerCase());
	}

	public boolean isPlayerMuted(String name) {
		name = name.toLowerCase();

		if (playerMutes.get(name) != null)
			return true;

		return dbLogger.isMuted(name);
	}

	public void addPlayerWarning(String name, String by, String reason) {
		dbLogger.logWarning(name, by, reason);
	}

	/*
	 * public void removePlayerWarning(String name) {
	 * 
	 * }
	 */

	public ArrayList<WarnData> getPlayerWarnings(String name) {
		return dbLogger.getWarnings(name.toLowerCase());
	}

	public String getPlayerIP(String name) {
		return dbLogger.getIP(name.toLowerCase());
	}

	public void setPlayerIP(String name, String ip) {
		dbLogger.setIP(name.toLowerCase(), ip);
	}
}
