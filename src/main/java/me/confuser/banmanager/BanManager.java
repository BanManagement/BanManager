package me.confuser.banmanager;

import java.io.IOException;
import java.sql.SQLException;

import org.mcstats.MetricsLite;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import me.confuser.banmanager.commands.*;
import me.confuser.banmanager.configs.*;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.listeners.*;
import me.confuser.banmanager.runnables.*;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.storage.conversion.UUIDConvert;
import me.confuser.banmanager.storage.mysql.MySQLDatabase;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.BukkitPlugin;

public class BanManager extends BukkitPlugin {
	private static BanManager statPlugin;

	private BanManager plugin;

	private JdbcPooledConnectionSource localConn;
	private JdbcPooledConnectionSource externalConn;
	private JdbcPooledConnectionSource conversionConn;

	private PlayerBanStorage playerBanStorage;
	private PlayerBanRecordStorage playerBanRecordStorage;
	private PlayerKickStorage playerKickStorage;
	private PlayerMuteStorage playerMuteStorage;
	private PlayerMuteRecordStorage playerMuteRecordStorage;
	private PlayerStorage playerStorage;
	private PlayerWarnStorage playerWarnStorage;

	private IpBanStorage ipBanStorage;
	private IpBanRecordStorage ipBanRecordStorage;

	private DefaultConfig config;
	private ConsoleConfig consoleConfig;
	private SchedulesConfig schedulesConfig;

	public void onEnable() {
		statPlugin = plugin = this;

		setupConfigs();
		try {
			if (!config.isDebugEnabled())
				disableDatabaseLogging();

			if (!setupConnections())
				return;

			setupStorages();
		} catch (SQLException e) {
			getLogger().warning("An error occurred attempting to make a database connection, please see stack trace below");
			plugin.getPluginLoader().disablePlugin(this);
			e.printStackTrace();
			return;
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

		if (conversionConn != null) {
			setupConversion();
		}

		setupListeners();
		setupCommands();
		setupRunnables();

		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}

	public void onDisable() {
		localConn.closeQuietly();

		if (externalConn != null) {
			externalConn.closeQuietly();
		}
	}

	public PlayerBanStorage getPlayerBanStorage() {
		return playerBanStorage;
	}

	public PlayerBanRecordStorage getPlayerBanRecordStorage() {
		return playerBanRecordStorage;
	}

	public PlayerKickStorage getPlayerKickStorage() {
		return playerKickStorage;
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

	public IpBanStorage getIpBanStorage() {
		return ipBanStorage;
	}

	public IpBanRecordStorage getIpBanRecordStorage() {
		return ipBanRecordStorage;
	}

	public DefaultConfig getDefaultConfig() {
		return config;
	}

	public ConsoleConfig getConsoleConfig() {
		return consoleConfig;
	}

	public SchedulesConfig getSchedulesConfig() {
		return schedulesConfig;
	}

	public JdbcPooledConnectionSource getLocalConnection() {
		return localConn;
	}

	public static BanManager getPlugin() {
		return statPlugin;
	}

	private void disableDatabaseLogging() {
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
	}

	private void setupConversion() {
		getLogger().info("Running pre-conversion launch checks");
		ConvertDatabaseConfig conversionDb = config.getConversionDb();

		if (config.getLocalDb().getHost().equals(conversionDb.getHost()) && config.getLocalDb().getName().equals(conversionDb.getName())) {
			if (!conversionChecks())
				return;
		}

		// Begin the converting
		getLogger().info("Conversion will begin shortly. You have 30 seconds to kill the process to abort.");
		try {
			Thread.sleep(30000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		getLogger().info("Launching conversion procedres");
		new UUIDConvert(conversionConn);
	}

	private boolean conversionChecks() {
		ConvertDatabaseConfig conversionDb = config.getConversionDb();

		if (playerStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("playerIpsTable"))) {
			getLogger().severe("players table equals playerIpsTable, aborting");
			return false;
		}

		if (playerBanStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("bansTable"))) {
			getLogger().severe("playerBans table equals bansTable, aborting");
			return false;
		}

		if (playerBanRecordStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("bansRecordTable"))) {
			getLogger().severe("playerBanRecords table equals bansRecordTable, aborting");
			return false;
		}

		if (playerMuteStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("mutesTable"))) {
			getLogger().severe("playerMutes table equals mutesTable, aborting");
			return false;
		}

		if (playerMuteRecordStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("mutesRecordTable"))) {
			getLogger().severe("playerMuteRecords table equals mutesRecordTable, aborting");
			return false;
		}

		if (playerKickStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("kicksTable"))) {
			getLogger().severe("playerKicks table equals kicksTable, aborting");
			return false;
		}

		if (playerWarnStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("warningsTable"))) {
			getLogger().severe("playerWarnings table equals warningsTable, aborting");
			return false;
		}

		if (ipBanStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("ipBansTable"))) {
			getLogger().severe("ipBans table equals ipBansTable, aborting");
			return false;
		}

		if (ipBanRecordStorage.getTableInfo().getTableName().equals(conversionDb.getTableName("ipBansRecordTable"))) {
			getLogger().severe("ipBanRecords table equals ipBansRecordTable, aborting");
			return false;
		}

		return true;
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

		// IP Bans
		new BanIpCommand().register();
		new TempIpBanCommand().register();
		new UnbanIpCommand().register();

		// Misc
		new ImportCommand().register();
		new FindAltsCommand().register();
		new ReloadCommand().register();
		new InfoCommand().register();

		// Kicks
		new KickCommand().register();
		new LoglessKickCommand().register();

		new WarnCommand().register();
	}

	@Override
	public void setupConfigs() {
		new MessagesConfig().load();

		config = new DefaultConfig();
		config.load();

		consoleConfig = new ConsoleConfig();
		consoleConfig.load();

		schedulesConfig = new SchedulesConfig();
		schedulesConfig.load();
	}

	public boolean setupConnections() throws SQLException {
		DatabaseConfig localDb = config.getLocalDb();

		if (!localDb.isEnabled()) {
			getLogger().warning("Local Database is not enabled, disabling plugin");
			plugin.getPluginLoader().disablePlugin(this);
			return false;
		}

		localConn = new JdbcPooledConnectionSource(localDb.getJDBCUrl());

		if (!localDb.getUser().isEmpty())
			localConn.setUsername(localDb.getUser());
		if (!localDb.getPassword().isEmpty())
		                localConn.setPassword(localDb.getPassword());

            localConn.setMaxConnectionsFree(localDb.getMaxConnections());
            /* There is a memory leak in ormlite-jbcd that means we should not use this. AutoReconnect handles this for us. */
            localConn.setTestBeforeGet(false);
            /* Keep the connection open for 15 minutes */
            localConn.setMaxConnectionAgeMillis(900000);
            /* We should not use this. Auto reconnect does this for us. Waste of packets and CPU. */
            localConn.setCheckConnectionsEveryMillis(0);
            localConn.setDatabaseType(new MySQLDatabase());
            localConn.initialize();

            if (!config.getConversionDb().isEnabled())
			return true;

		DatabaseConfig conversionDb = config.getConversionDb();

		conversionConn = new JdbcPooledConnectionSource(conversionDb.getJDBCUrl());

		if (!conversionDb.getUser().isEmpty())
			conversionConn.setUsername(conversionDb.getUser());
		if (!conversionDb.getPassword().isEmpty())
			conversionConn.setPassword(conversionDb.getPassword());

		conversionConn.setMaxConnectionsFree(conversionDb.getMaxConnections());
            /* There is a memory leak in ormlite-jbcd that means we should not use this. AutoReconnect handles this for us. */
		conversionConn.setTestBeforeGet(false);
		/* Keep the connection open for 15 minutes */
		conversionConn.setMaxConnectionAgeMillis(900000);
            /* We should not use this. Auto reconnect does this for us. Waste of packets and CPU. */
            conversionConn.setCheckConnectionsEveryMillis(0);
		conversionConn.setDatabaseType(new MySQLDatabase());
            conversionConn.initialize();
            
		return true;
	}

	@SuppressWarnings("unchecked")
	public void setupStorages() throws SQLException {
		DatabaseTableConfig<PlayerData> playerConfig = (DatabaseTableConfig<PlayerData>) config.getLocalDb().getTable("players");
		playerStorage = new PlayerStorage(localConn, playerConfig);

		if (!playerStorage.isTableExists())
			TableUtils.createTable(localConn, playerConfig);
		
		playerStorage.setupConsole();

		DatabaseTableConfig<PlayerBanData> playerBansConfig = (DatabaseTableConfig<PlayerBanData>) config.getLocalDb().getTable("playerBans");
		playerBanStorage = new PlayerBanStorage(localConn, playerBansConfig);

		if (!playerBanStorage.isTableExists())
			TableUtils.createTable(localConn, playerBansConfig);

		DatabaseTableConfig<PlayerBanRecord> playerBanRecordsConfig = (DatabaseTableConfig<PlayerBanRecord>) config.getLocalDb().getTable("playerBanRecords");
		playerBanRecordStorage = new PlayerBanRecordStorage(localConn, playerBanRecordsConfig);

		if (!playerBanRecordStorage.isTableExists())
			TableUtils.createTable(localConn, playerBanRecordsConfig);

		DatabaseTableConfig<PlayerMuteData> playerMutesConfig = (DatabaseTableConfig<PlayerMuteData>) config.getLocalDb().getTable("playerMutes");
		playerMuteStorage = new PlayerMuteStorage(localConn, playerMutesConfig);

		if (!playerMuteStorage.isTableExists())
			TableUtils.createTable(localConn, playerMutesConfig);

		DatabaseTableConfig<PlayerMuteRecord> playerMuteRecordsConfig = (DatabaseTableConfig<PlayerMuteRecord>) config.getLocalDb().getTable("playerMuteRecords");
		playerMuteRecordStorage = new PlayerMuteRecordStorage(localConn, playerMuteRecordsConfig);

		if (!playerMuteRecordStorage.isTableExists())
			TableUtils.createTable(localConn, playerMuteRecordsConfig);

		DatabaseTableConfig<PlayerWarnData> playerWarningsConfig = (DatabaseTableConfig<PlayerWarnData>) config.getLocalDb().getTable("playerWarnings");
		playerWarnStorage = new PlayerWarnStorage(localConn, playerWarningsConfig);

		if (!playerWarnStorage.isTableExists())
			TableUtils.createTable(localConn, playerWarningsConfig);

		DatabaseTableConfig<PlayerKickData> playerKickConfig = (DatabaseTableConfig<PlayerKickData>) config.getLocalDb().getTable("playerKicks");
		playerKickStorage = new PlayerKickStorage(localConn, playerKickConfig);

		if (!playerKickStorage.isTableExists())
			TableUtils.createTable(localConn, playerKickConfig);

		DatabaseTableConfig<IpBanData> ipBansConfig = (DatabaseTableConfig<IpBanData>) config.getLocalDb().getTable("ipBans");
		ipBanStorage = new IpBanStorage(localConn, ipBansConfig);

		if (!ipBanStorage.isTableExists())
			TableUtils.createTable(localConn, ipBansConfig);

		DatabaseTableConfig<IpBanRecord> ipBanRecordsConfig = (DatabaseTableConfig<IpBanRecord>) config.getLocalDb().getTable("ipBanRecords");
		ipBanRecordStorage = new IpBanRecordStorage(localConn, ipBanRecordsConfig);

		if (!ipBanRecordStorage.isTableExists())
			TableUtils.createTable(localConn, ipBanRecordsConfig);
	}

	@Override
	public void setupListeners() {
		new JoinListener().register();
		new LeaveListener().register();
		new ChatListener().register();
		new CommandListener().register();
	}

	@Override
	public void setupRunnables() {
		if (schedulesConfig.getSchedule("playerBans") != 0)
			getServer().getScheduler().runTaskTimerAsynchronously(plugin, new BanSync(), schedulesConfig.getSchedule("playerBans"), schedulesConfig.getSchedule("playerBans"));

		if (schedulesConfig.getSchedule("playerMutes") != 0)
			getServer().getScheduler().runTaskTimerAsynchronously(plugin, new MuteSync(), schedulesConfig.getSchedule("playerMutes"), schedulesConfig.getSchedule("playerMutes"));

		if (schedulesConfig.getSchedule("ipBans") != 0)
			getServer().getScheduler().runTaskTimerAsynchronously(plugin, new IpSync(), schedulesConfig.getSchedule("ipBans"), schedulesConfig.getSchedule("ipBans"));

		if (schedulesConfig.getSchedule("expiresCheck") != 0)
			getServer().getScheduler().runTaskTimerAsynchronously(plugin, new ExpiresSync(), schedulesConfig.getSchedule("expiresCheck"), schedulesConfig.getSchedule("expiresCheck"));
	
            /* Rgus task should be ran last with a 1L offset as it gets modified above. */
            getServer().getScheduler().runTaskTimerAsynchronously(plugin, new SaveLastChecked(), (schedulesConfig.getSchedule("saveLastChecked") + 1L), (schedulesConfig.getSchedule("saveLastChecked") + 1L));
      }
}
