package me.confuser.banmanager;

import java.sql.SQLException;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

import me.confuser.banmanager.commands.*;
import me.confuser.banmanager.configs.*;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.listeners.*;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.BukkitPlugin;

public class BanManager extends BukkitPlugin {
	private static BanManager statPlugin;
	
	private BanManager plugin;
	
	private JdbcPooledConnectionSource localConn;
	private JdbcPooledConnectionSource externalConn;
	private PlayerBanStorage playerBanStorage;
	private PlayerBanRecordStorage playerBanRecordStorage;
	private KickStorage kickStorage;
	private PlayerMuteStorage playerMuteStorage;
	private PlayerMuteRecordStorage playerMuteRecordStorage;
	private PlayerStorage playerStorage;
	private PlayerWarnStorage playerWarnStorage;
	
	private DefaultConfig config;
	
	public void onEnable() {
		statPlugin = plugin = this;
		
		setupConfigs();
		try {
			setupConnections();
			setupStorages();
		} catch (SQLException e) {
			getLogger().warning("An error occurred attempting to make a database connection, please see stack trace below");
			plugin.getPluginLoader().disablePlugin(this);
			e.printStackTrace();
		}
		
		try {
			long timeDiff = DateUtils.findTimeDiff();
			
			if (timeDiff > 1) {
				getLogger().severe("The time on your server and MySQL database are out by " + timeDiff + " seconds, this may cause syncing issues.");
			}
		} catch (SQLException e) {
			getLogger().warning("An error occurred attempting to find the time difference, please see stack trace below");
			plugin.getPluginLoader().disablePlugin(this);
			e.printStackTrace();
		}

		setupListeners();
		setupCommands();
		setupRunnables();
	}
	
	public PlayerBanStorage getPlayerBanStorage() {
		return playerBanStorage;
	}
	
	public PlayerBanRecordStorage getPlayerBanRecordStorage() {
		return playerBanRecordStorage;
	}
	
	public PlayerMuteStorage getPlayerMuteStorage() {
		return playerMuteStorage;
	}
	
	public PlayerMuteRecordStorage getPlayerMuteRecordStorage() {
		return playerMuteRecordStorage;
	}
	
	public PlayerWarnStorage getPlayerWarnStorage() {
		return playerWarnStorage;
	}
	
	public PlayerStorage getPlayerStorage() {
		return playerStorage;
	}
	
	public DefaultConfig getDefaultConfig() {
		return config;
	}
	
	public JdbcPooledConnectionSource getLocalConnection() {
		return localConn;
	}
	
	public static BanManager getPlugin() {
		return statPlugin;
	}

	@Override
	public String getPermissionBase() {
		return "bm";
	}

	@Override
	public String getPluginFriendlyName() {
		return "BanManager";
	}

	@Override
	public void setupCommands() {
		// Player bans
		new BanCommand().register();
		new TempBanCommand().register();
		new UnbanCommand().register();
		
		// Player mutes
		new MuteCommand().register();
		new TempMuteCommand().register();
		new UnmuteCommand().register();
	}

	@Override
	public void setupConfigs() {
		new MessagesConfig().load();
		
		config = new DefaultConfig();
		config.load();
	}
	
	public void setupConnections() throws SQLException {
		DatabaseConfig localDb = config.getLocalDb();
		
		if (!localDb.isEnabled()) {
			getLogger().warning("Local Database is not enabled, disabling plugin");
			plugin.getPluginLoader().disablePlugin(this);
			return;
		}
		
		localConn = new JdbcPooledConnectionSource(localDb.getJDBCUrl());
		
		if (!localDb.getUser().isEmpty())
			localConn.setUsername(localDb.getUser());
		if (!localDb.getPassword().isEmpty())
			localConn.setPassword(localDb.getPassword());
		
		// TODO config
		localConn.setMaxConnectionsFree(10);
		localConn.setTestBeforeGet(true);
		// only keep the connections open for 5 minutes
		localConn.setMaxConnectionAgeMillis(5 * 60 * 1000);
	}
	
	@SuppressWarnings("unchecked")
	public void setupStorages() throws SQLException {
		playerStorage = new PlayerStorage(localConn, (DatabaseTableConfig<PlayerData>) config.getLocalDb().getTable("players"));
		playerBanStorage = new PlayerBanStorage(localConn, (DatabaseTableConfig<PlayerBanData>) config.getLocalDb().getTable("bans"));
		playerBanRecordStorage = new PlayerBanRecordStorage(localConn, (DatabaseTableConfig<PlayerBanRecord>) config.getLocalDb().getTable("playerBanRecords"));
		playerMuteStorage = new PlayerMuteStorage(localConn, (DatabaseTableConfig<PlayerMuteData>) config.getLocalDb().getTable("mutes"));
		playerMuteRecordStorage = new PlayerMuteRecordStorage(localConn, (DatabaseTableConfig<PlayerMuteRecord>) config.getLocalDb().getTable("playerMuteRecords"));
	}

	@Override
	public void setupListeners() {
		new JoinListener().register();
		new LeaveListener().register();
		new ChatListener().register();
		new CommandListener().register();
		
		// TODO Make optional in config
		new ConsoleListener().register();
	}

	@Override
	public void setupRunnables() {
		// TODO Auto-generated method stub
		
	}
}
