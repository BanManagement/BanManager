package me.confuserr.banmanager;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.confuserr.banmanager.Database;
import me.confuserr.banmanager.Commands.BanCommand;
import me.confuserr.banmanager.Commands.BanImportCommand;
import me.confuserr.banmanager.Commands.BanIpCommand;
import me.confuserr.banmanager.Commands.BmInfoCommand;
import me.confuserr.banmanager.Commands.KickCommand;
import me.confuserr.banmanager.Commands.LoglessKickCommand;
import me.confuserr.banmanager.Commands.MuteCommand;
import me.confuserr.banmanager.Commands.ReloadCommand;
import me.confuserr.banmanager.Commands.TempBanCommand;
import me.confuserr.banmanager.Commands.TempMuteCommand;
import me.confuserr.banmanager.Commands.UnBanCommand;
import me.confuserr.banmanager.Commands.UnBanIpCommand;
import me.confuserr.banmanager.Commands.UnMuteCommand;
import me.confuserr.banmanager.listeners.AsyncChat;
import me.confuserr.banmanager.listeners.AsyncPreLogin;
import me.confuserr.banmanager.listeners.MutedBlacklistCheck;
import me.confuserr.banmanager.listeners.SyncChat;
import me.confuserr.banmanager.listeners.SyncLogin;
import me.confuserr.banmanager.listeners.UpdateNotify;
import me.confuserr.banmanager.scheduler.bukkitUnbanSync;
import me.confuserr.banmanager.scheduler.databaseAsync;
import me.confuserr.banmanager.scheduler.databaseClose;
import me.confuserr.banmanager.scheduler.muteAsync;
import net.h31ix.updater.Updater;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class BanManager extends JavaPlugin {
	public Logger logger = Logger.getLogger("Minecraft");
	public BanManager plugin;
	public String localUser;
	public String localPass;
	public String localHost;
	public String localDatabase;
	public String localPort;
	public String localUrl;
	public String localBansTable;
	public String localBanRecordTable;
	public Database localConn;
	public String localIpBansTable;
	public String localIpBanRecordTable;
	public String localKicksTable;
	public String localMutesTable;
	public String localMutesRecordTable;
	public String localPlayerIpsTable;
	public String serverName;
	public boolean importInProgress = false;
	public Map<String, String> banMessages = new HashMap<String, String>();
	public boolean logKicks;
	public List<String> toUnbanPlayer = Collections.synchronizedList(new ArrayList<String>());
	public List<String> toUnbanIp = Collections.synchronizedList(new ArrayList<String>());
	public int keepKicks;
	public boolean onlineMode;
	public boolean checkForUpdates = true;
	public boolean updateAvailable = false;
	public List<String> mutedBlacklist;
	public HashMap<String, String> timeLimitsMutes = new HashMap<String, String>();
	public HashMap<String, String> timeLimitsBans = new HashMap<String, String>();
	
	public DbLogger dbLogger;
	public String updateVersion;
	
	public ConcurrentHashMap<String, Long> mutedPlayersLength = new ConcurrentHashMap<String, Long>();
	public ConcurrentHashMap<String, String> mutedPlayersReason = new ConcurrentHashMap<String, String>();
	public ConcurrentHashMap<String, String> mutedPlayersBy = new ConcurrentHashMap<String, String>();
	
	public boolean usePartialNames = true;
	
	@Override
	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		getServer().getScheduler().cancelTasks(this);
		this.logger.info("["+pdfFile.getName() + "] has been disabled");
		localConn.close();
	}

	@Override
	public void onEnable() {
		this.getConfig().options().copyDefaults(true);
		this.saveConfig();
		plugin = this;
		
		PluginDescriptionFile pdfFile = this.getDescription();
		
		if(getConfig().getString("localDatabase.url") != null) {
			// Old config, need to migrate!
			
			String oldUrl = getConfig().getString("localDatabase.url");

			Pattern p = Pattern.compile("jdbc:mysql:\\/\\/(.*):(.*)\\/(.*)");
			Matcher m = p.matcher(oldUrl);
			
			while (m.find()) {
				localHost = m.group(1);
				localPort = m.group(2);
				localDatabase = m.group(3);
			}
			
			this.logger.info("["+pdfFile.getName()+"] Old config found, migrating!");

			getConfig().set("localDatabase.url", null);
			
			getConfig().set("localDatabase.host", localHost);
			getConfig().set("localDatabase.port", localPort);
			getConfig().set("localDatabase.database", localDatabase);
			
			this.saveConfig();
		}
		
		// Set the database variables from the config
		localHost = getConfig().getString("localDatabase.host");
		localPort = getConfig().getString("localDatabase.port");
		localDatabase = getConfig().getString("localDatabase.database");
		localUser = getConfig().getString("localDatabase.username");
		localPass = getConfig().getString("localDatabase.password");
		
		//localUrl  = getConfig().getString("localDatabase.url");
		localUrl = "jdbc:mysql://"+localHost+":"+localPort+"/"+localDatabase+"?autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
		
		localBansTable = getConfig().getString("localDatabase.bansTable");
		localBanRecordTable = getConfig().getString("localDatabase.bansRecordTable");
		
		localIpBansTable = getConfig().getString("localDatabase.ipBansTable");
		localIpBanRecordTable = getConfig().getString("localDatabase.ipBansRecordTable");
		
		localKicksTable = getConfig().getString("localDatabase.kicksTable");
		
		localMutesTable = getConfig().getString("localDatabase.mutesTable");
		localMutesRecordTable = getConfig().getString("localDatabase.mutesRecordTable");
		
		localPlayerIpsTable = getConfig().getString("localDatabase.playerIpsTable");
		
		logKicks = getConfig().getBoolean("logKicks");
		keepKicks = getConfig().getInt("keepKicks");
		
		serverName = getConfig().getString("serverName");
		
		checkForUpdates = getConfig().getBoolean("checkForUpdates");
		
		usePartialNames = getConfig().getBoolean("use-partial-names");
		
		for(String key : getConfig().getConfigurationSection("messages").getKeys(false)) {
	    	banMessages.put(key, colorize(getConfig().getString("messages."+key).replace("\\n", "\n")));
		}
		
		mutedBlacklist = getConfig().getStringList("mutedCommandBlacklist");
		
		// Loop through the time limits
		for(String key : getConfig().getConfigurationSection("timeLimits.mutes").getKeys(false)) {
			String path = "timeLimits.mutes."+key;
			timeLimitsMutes.put(key, getConfig().getString(path));
		}
		
		for(String key : getConfig().getConfigurationSection("timeLimits.bans").getKeys(false)) {
			String path = "timeLimits.bans."+key;
			timeLimitsBans.put(key, getConfig().getString(path));
		}
		
		localConn = new Database(localUser, localPass, localUrl, this);
		
		plugin.dbLogger = new DbLogger(localConn, localBansTable, localBanRecordTable, localIpBansTable, localIpBanRecordTable, plugin);
		
		if(!localConn.checkConnection()) {
			this.logger.info("["+pdfFile.getName()+"] is unable to connect to the database, it has been disabled");
			plugin.getPluginLoader().disablePlugin(this);
			return;
		}
		
		if(!localConn.checkTable(localPlayerIpsTable)) {
			this.logger.info("["+pdfFile.getName()+"] creating tables");
			try {
				plugin.dbLogger.create_tables();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} else {
			// Check to see if they need updating to include the new server column
			plugin.dbLogger.serverExists();
		}
		localConn.close();
		
		getCommand("ban").setExecutor(new BanCommand(this));
		getCommand("tempban").setExecutor(new TempBanCommand(this));
		getCommand("unban").setExecutor(new UnBanCommand(this));
		getCommand("bminfo").setExecutor(new BmInfoCommand(this));
		getCommand("banip").setExecutor(new BanIpCommand(this));
		getCommand("unbanip").setExecutor(new UnBanIpCommand(this));
		getCommand("banimport").setExecutor(new BanImportCommand(this));
		getCommand("kick").setExecutor(new KickCommand(this));
		getCommand("nlkick").setExecutor(new LoglessKickCommand(this));
		getCommand("mute").setExecutor(new MuteCommand(this));
		getCommand("tempmute").setExecutor(new TempMuteCommand(this));
		getCommand("unmute").setExecutor(new UnMuteCommand(this));
		getCommand("bmreload").setExecutor(new ReloadCommand(this));
		
		// Is the server in online mode?
		onlineMode = getServer().getOnlineMode();
		
		if (getConfig().getBoolean("useSyncChat")) { // If syncChat is on, use Sync events
			getServer().getPluginManager().registerEvents(new SyncLogin(plugin), this);
			getServer().getPluginManager().registerEvents(new SyncChat(plugin), this);
		}
		else if(onlineMode) { // If server is in online mode and syncChat is off, use async events
			getServer().getPluginManager().registerEvents(new AsyncPreLogin(plugin), this);
			getServer().getPluginManager().registerEvents(new AsyncChat(plugin), this);
		}
		else { // Otherwise use the normal sync login event and use Async chat even for mutes.
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
		
		this.logger.info("["+pdfFile.getName()+"] Version:" + pdfFile.getVersion() + " has been enabled");

		
		// Checks for expired bans, and moves them into the record table
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new databaseAsync(this), 2400L, 6000L); // 2 minute delay before it starts, runs every 5 minutes
		
		// Bukkit unban bans those that have expired
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new bukkitUnbanSync(this), 2420L, 6040L);
		
		// Check the muted table for new mutes
		this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new muteAsync(this), 20L, 150L);
		
		// Close the MySQL connection every so often rather than constantly opening and closing it.
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new databaseClose(this), 2425L, 2400L);
		
		// Check for an update
		if(checkForUpdates) {
			Updater updater = new Updater(this, "ban-management", this.getFile(), Updater.UpdateType.NO_DOWNLOAD, false);
			updateAvailable = updater.getResult() == Updater.UpdateResult.UPDATE_AVAILABLE; // Determine if there is an update ready for us
			updateVersion = updater.getLatestVersionString(); // Get the latest version
			if(updateAvailable) {
				this.logger.info("["+pdfFile.getName()+"] "+updateVersion+" update available");
				getServer().getPluginManager().registerEvents(new UpdateNotify(plugin), this);
			}
		}
	}
	
	// Reloads everything in the config except the database details
	public void configReload() {
		logKicks = getConfig().getBoolean("logKicks");
		keepKicks = getConfig().getInt("keepKicks");
		
		serverName = getConfig().getString("serverName");
		
		checkForUpdates = getConfig().getBoolean("checkForUpdates");
		
		usePartialNames = getConfig().getBoolean("use-partial-names");
		
		
		banMessages.clear();
		
		for(String key : getConfig().getConfigurationSection("messages").getKeys(false)) {
	    	banMessages.put(key, colorize(getConfig().getString("messages."+key).replace("\\n", "\n")));
		}
		
		mutedBlacklist = getConfig().getStringList("mutedCommandBlacklist");
		
		
		timeLimitsMutes.clear();
		// Loop through the time limits
		for(String key : getConfig().getConfigurationSection("timeLimits.mutes").getKeys(false)) {
			String path = "timeLimits.mutes."+key;
			timeLimitsMutes.put(key, getConfig().getString(path));
		}
		
		timeLimitsBans.clear();
		for(String key : getConfig().getConfigurationSection("timeLimits.bans").getKeys(false)) {
			String path = "timeLimits.bans."+key;
			timeLimitsBans.put(key, getConfig().getString(path));
		}
	}
	
	public void sendMessage(CommandSender sender, String message) {
		if(sender instanceof Player) {
			Player player = (Player) sender;
			String[] s = message.split("\\\\n");
		    for (String m : s) {
		    	player.sendMessage(m);
		    }
		} else {
			String[] s = message.split("\\\\n");
		    for (String m : s) {
		    	sender.sendMessage(m);
		    }
		}
	}
	
	public void sendMessageWithPerm(String message, String perm) {
		for(Player onlinePlayer : getServer().getOnlinePlayers()) {
			if(onlinePlayer.hasPermission(perm))
				onlinePlayer.sendMessage(message);
		}
	}
	
	public String viewReason(String reason) {
		return reason.replace("&quot;", "\"").replace("&#039;", "'");
	}
	
	public String parseReason(String[] args) {
		String reason = plugin.implodeArray(args, " ").replace("\"", "&quot;").replace("'", "&#039;");
		return reason;
	}
	
	public static String colorize(String string) {
		return string.replaceAll("(?i)&([a-k0-9])", "\u00A7$1");
	}
	
	public String implodeArray(String[] inputArray, String glueString) {
		/** Output variable */
		String output = "";
	
		if (inputArray.length > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(inputArray[0]);
	
			for (int i=1; i<inputArray.length; i++) {
				sb.append(glueString);
				sb.append(inputArray[i]);
			}
	
			output = sb.toString();
		}
	
		return output;
	}
	
	public String getReason(String[] args, int start) {
		// Basically ignore the first item in the array
		String[] newArgs = new String[args.length - start];
		System.arraycopy(args, start, newArgs, 0, args.length - start);
		return plugin.parseReason(newArgs);
	}
	
	public final static boolean ValidateIPAddress( String  ipAddress ) {
	    String[] parts = ipAddress.split( "\\." );

	    if ( parts.length != 4 )
	        return false;

	    for ( String s : parts ) {
	        int i = Integer.parseInt( s );

	        if ( (i < 0) || (i > 255) )
	            return false;
	    }

	    return true;
	}
	
	public final String getIp(InetAddress ip) {
		return ip.toString().replace("/", "");
	}
	
	public String getIp(String ip) {
		ip = ip.replace("/", "");
		
		String[] withoutPort = ip.split(":");
		
		if(withoutPort.length == 2)
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
		if(plugin.mutedPlayersBy.containsKey(player)) {
			plugin.mutedPlayersBy.remove(player);
			plugin.mutedPlayersLength.remove(player);
			plugin.mutedPlayersReason.remove(player);
		}
	}
	
	public void removeMute(String player, String by) {
		plugin.dbLogger.muteRemove(player, by);
		if(plugin.mutedPlayersBy.containsKey(player)) {
			plugin.mutedPlayersBy.remove(player);
			plugin.mutedPlayersLength.remove(player);
			plugin.mutedPlayersReason.remove(player);
		}
	}
	
	public void removeHashMute(String player) {
		if(plugin.mutedPlayersBy.containsKey(player)) {
			plugin.mutedPlayersBy.remove(player);
			plugin.mutedPlayersLength.remove(player);
			plugin.mutedPlayersReason.remove(player);
		}
	}
	
	// Copyright essentials, all credits to them, this is here to remove dependency on it, I did not create these functions!
		public long parseDateDiff(String time, boolean future) throws Exception {
			Pattern timePattern = Pattern.compile(
					"(?:([0-9]+)\\s*y[a-z]*[,\\s]*)?"
					+ "(?:([0-9]+)\\s*mo[a-z]*[,\\s]*)?"
					+ "(?:([0-9]+)\\s*w[a-z]*[,\\s]*)?"
					+ "(?:([0-9]+)\\s*d[a-z]*[,\\s]*)?"
					+ "(?:([0-9]+)\\s*h[a-z]*[,\\s]*)?"
					+ "(?:([0-9]+)\\s*m[a-z]*[,\\s]*)?"
					+ "(?:([0-9]+)\\s*(?:s[a-z]*)?)?", Pattern.CASE_INSENSITIVE);
			Matcher m = timePattern.matcher(time);
			int years = 0;
			int months = 0;
			int weeks = 0;
			int days = 0;
			int hours = 0;
			int minutes = 0;
			int seconds = 0;
			boolean found = false;
			while (m.find())
			{
				if (m.group() == null || m.group().isEmpty())
				{
					continue;
				}
				for (int i = 0; i < m.groupCount(); i++)
				{
					if (m.group(i) != null && !m.group(i).isEmpty())
					{
						found = true;
						break;
					}
				}
				if (found)
				{
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
		
		public String formatDateDiff(Calendar fromDate, Calendar toDate)
		{
			boolean future = false;
			if (toDate.equals(fromDate))
			{
				return "now";
			}
			if (toDate.after(fromDate))
			{
				future = true;
			}

			StringBuilder sb = new StringBuilder();
			int[] types = new int[]
			{
				Calendar.YEAR,
				Calendar.MONTH,
				Calendar.DAY_OF_MONTH,
				Calendar.HOUR_OF_DAY,
				Calendar.MINUTE,
				Calendar.SECOND
			};
			String[] names = new String[]
			{
				plugin.banMessages.get("timeYear"),
				plugin.banMessages.get("timeYears"),
				plugin.banMessages.get("timeMonth"),
				plugin.banMessages.get("timeMonths"),
				plugin.banMessages.get("timeDay"),
				plugin.banMessages.get("timeDays"),
				plugin.banMessages.get("timeHour"),
				plugin.banMessages.get("timeHours"),
				plugin.banMessages.get("timeMinute"),
				plugin.banMessages.get("timeMinutes"),
				plugin.banMessages.get("timeSecond"),
				plugin.banMessages.get("timeSeconds")
			};
			for (int i = 0; i < types.length; i++)
			{
				int diff = dateDiff(types[i], fromDate, toDate, future);
				if (diff > 0)
				{
					sb.append(" ").append(diff).append(" ").append(names[i * 2 + (diff > 1 ? 1 : 0)]);
				}
			}
			if (sb.length() == 0)
			{
				return "now";
			}
			return sb.toString();
		}
		
		public String formatDateDiff(long date)
		{
			Calendar c = new GregorianCalendar();
			c.setTimeInMillis(date);
			Calendar now = new GregorianCalendar();
			return formatDateDiff(now, c);
		}
		
		private static int dateDiff(int type, Calendar fromDate, Calendar toDate, boolean future)
		{
			int diff = 0;
			long savedDate = fromDate.getTimeInMillis();
			while ((future && !fromDate.after(toDate)) || (!future && !fromDate.before(toDate)))
			{
				savedDate = fromDate.getTimeInMillis();
				fromDate.add(type, future ? 1 : -1);
				diff++;
			}
			diff--;
			fromDate.setTimeInMillis(savedDate);
			return diff;
		}
}
