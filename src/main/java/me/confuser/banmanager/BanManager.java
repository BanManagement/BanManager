package me.confuser.banmanager;

import java.sql.SQLException;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;

import me.confuser.banmanager.commands.*;
import me.confuser.banmanager.configs.*;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.listeners.*;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.storage.mysql.MySQLDatabase;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.BukkitPlugin;

public class BanManager extends BukkitPlugin {
	private static BanManager statPlugin;

	private BanManager plugin;

	private JdbcPooledConnectionSource localConn;
	private JdbcPooledConnectionSource externalConn;

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

	public JdbcPooledConnectionSource getLocalConnection() {
		return localConn;
	}

	public static BanManager getPlugin() {
		return statPlugin;
	}
	
	private void disableDatabaseLogging() {
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
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

		new ImportCommand().register();
		new FindAltsCommand().register();
		new ReloadCommand().register();
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
		localConn.setTestBeforeGet(true);
		// only keep the connections open for 5 minutes
		localConn.setMaxConnectionAgeMillis(5 * 60 * 1000);
		localConn.setDatabaseType(new MySQLDatabase());

		localConn.initialize();

		return true;
	}

	@SuppressWarnings("unchecked")
	public void setupStorages() throws SQLException {
		DatabaseTableConfig<PlayerData> playerConfig = (DatabaseTableConfig<PlayerData>) config.getLocalDb().getTable("players");
		playerStorage = new PlayerStorage(localConn, playerConfig);

		if (!playerStorage.isTableExists())
			TableUtils.createTable(localConn, playerConfig);

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
		// TODO Auto-generated method stub

	}
}
