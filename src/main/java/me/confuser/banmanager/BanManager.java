package me.confuser.banmanager;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import me.confuser.banmanager.commands.*;
import me.confuser.banmanager.commands.external.*;
import me.confuser.banmanager.configs.*;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.data.external.*;
import me.confuser.banmanager.listeners.*;
import me.confuser.banmanager.runnables.*;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.storage.conversion.UUIDConvert;
import me.confuser.banmanager.storage.external.*;
import me.confuser.banmanager.storage.mysql.MySQLDatabase;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.BukkitPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;
import java.sql.SQLException;

public class BanManager extends BukkitPlugin {

  @Getter
  public static BanManager plugin;

  private JdbcPooledConnectionSource localConn;
  private JdbcPooledConnectionSource externalConn;
  private JdbcPooledConnectionSource conversionConn;

  @Getter
  private PlayerBanStorage playerBanStorage;
  @Getter
  private PlayerBanRecordStorage playerBanRecordStorage;
  @Getter
  private PlayerKickStorage playerKickStorage;
  @Getter
  private PlayerMuteStorage playerMuteStorage;
  @Getter
  private PlayerMuteRecordStorage playerMuteRecordStorage;
  @Getter
  private PlayerStorage playerStorage;
  @Getter
  private PlayerWarnStorage playerWarnStorage;

  @Getter
  private IpBanStorage ipBanStorage;
  @Getter
  private IpBanRecordStorage ipBanRecordStorage;

  @Getter
  private ExternalPlayerBanStorage externalPlayerBanStorage;
  @Getter
  private ExternalPlayerBanRecordStorage externalPlayerBanRecordStorage;
  @Getter
  private ExternalPlayerMuteStorage externalPlayerMuteStorage;
  @Getter
  private ExternalPlayerMuteRecordStorage externalPlayerMuteRecordStorage;

  @Getter
  private ExternalIpBanStorage externalIpBanStorage;
  @Getter
  private ExternalIpBanRecordStorage externalIpBanRecordStorage;

  @Getter
  private DefaultConfig configuration;
  @Getter
  private ConsoleConfig consoleConfig;
  @Getter
  private SchedulesConfig schedulesConfig;

  @Override
  public void onEnable() {
    plugin = this;

    setupConfigs();
    try {
      if (!configuration.isDebugEnabled()) {
        disableDatabaseLogging();
      }

      if (!setupConnections()) {
        return;
      }

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
        getLogger()
                .severe("The time on your server and MySQL database are out by " + timeDiff + " seconds, this may cause syncing issues.");
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

  @Override
  public void onDisable() {
    if (localConn != null) {
      localConn.closeQuietly();
    }

    if (externalConn != null) {
      externalConn.closeQuietly();
    }

    if (conversionConn != null) {
      conversionConn.closeQuietly();
    }
  }

  private void disableDatabaseLogging() {
    System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
  }

  private void setupConversion() {
    getLogger().info("Running pre-conversion launch checks");
    ConvertDatabaseConfig conversionDb = configuration.getConversionDb();

    if (configuration.getLocalDb().getHost().equals(conversionDb.getHost()) && configuration.getLocalDb().getName()
                                                                                            .equals(conversionDb
                                                                                                    .getName())) {
      if (!conversionChecks()) {
        return;
      }
    }

    // Begin the converting
    getLogger().info("Conversion will begin shortly. You have 30 seconds to kill the process to abort.");
    try {
      Thread.sleep(30000L);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    getLogger().info("Launching conversion process");
    new UUIDConvert(conversionConn);
  }

  private boolean conversionChecks() {
    ConvertDatabaseConfig conversionDb = configuration.getConversionDb();

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

    if (externalConn == null) return;

    new BanAllCommand().register();
    new TempBanAllCommand().register();
    new UnbanAllCommand().register();

    new MuteAllCommand().register();
    new TempMuteAllCommand().register();
    new UnmuteAllCommand().register();

    new BanIpAllCommand().register();
    new TempBanIpAllCommand().register();
    new UnbanIpAllCommand().register();
  }

  @Override
  public void setupConfigs() {
    new MessagesConfig().load();

    configuration = new DefaultConfig();
    configuration.load();

    consoleConfig = new ConsoleConfig();
    consoleConfig.load();

    schedulesConfig = new SchedulesConfig();
    schedulesConfig.load();
  }

  public boolean setupConnections() throws SQLException {
    if (!configuration.getLocalDb().isEnabled()) {
      getLogger().warning("Local Database is not enabled, disabling plugin");
      plugin.getPluginLoader().disablePlugin(this);
      return false;
    }

    localConn = setupConnection(configuration.getLocalDb());

    if (configuration.getConversionDb().isEnabled()) {
      conversionConn = setupConnection(configuration.getConversionDb());
    }

    if (configuration.getExternalDb().isEnabled()) {
      externalConn = setupConnection(configuration.getExternalDb());
    }

    return true;
  }

  private JdbcPooledConnectionSource setupConnection(DatabaseConfig dbConfig) throws SQLException {
    JdbcPooledConnectionSource connection = new JdbcPooledConnectionSource(dbConfig.getJDBCUrl());

    if (!dbConfig.getUser().isEmpty()) {
      connection.setUsername(dbConfig.getUser());
    }
    if (!dbConfig.getPassword().isEmpty()) {
      connection.setPassword(dbConfig.getPassword());
    }

    connection.setMaxConnectionsFree(dbConfig.getMaxConnections());
    /*
     * There is a memory leak in ormlite-jbcd that means we should not use
     * this. AutoReconnect handles this for us.
     */
    connection.setTestBeforeGet(false);
    /* Keep the connection open for 15 minutes */
    connection.setMaxConnectionAgeMillis(900000);
    /*
     * We should not use this. Auto reconnect does this for us. Waste of
     * packets and CPU.
     */
    connection.setCheckConnectionsEveryMillis(0);
    connection.setDatabaseType(new MySQLDatabase());
    connection.initialize();

    return connection;
  }

  @SuppressWarnings("unchecked")
  public void setupStorages() throws SQLException {
    // TODO Refactor this
    DatabaseTableConfig<PlayerData> playerConfig = (DatabaseTableConfig<PlayerData>) configuration.getLocalDb()
                                                                                                  .getTable("players");
    playerStorage = new PlayerStorage(localConn, playerConfig);

    if (!playerStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerConfig);
    }

    playerStorage.setupConsole();

    DatabaseTableConfig<PlayerBanData> playerBansConfig = (DatabaseTableConfig<PlayerBanData>) configuration
            .getLocalDb().getTable("playerBans");
    playerBanStorage = new PlayerBanStorage(localConn, playerBansConfig);

    if (!playerBanStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerBansConfig);
    }

    DatabaseTableConfig<PlayerBanRecord> playerBanRecordsConfig = (DatabaseTableConfig<PlayerBanRecord>) configuration
            .getLocalDb().getTable("playerBanRecords");
    playerBanRecordStorage = new PlayerBanRecordStorage(localConn, playerBanRecordsConfig);

    if (!playerBanRecordStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerBanRecordsConfig);
    }

    DatabaseTableConfig<PlayerMuteData> playerMutesConfig = (DatabaseTableConfig<PlayerMuteData>) configuration
            .getLocalDb().getTable("playerMutes");
    playerMuteStorage = new PlayerMuteStorage(localConn, playerMutesConfig);

    if (!playerMuteStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerMutesConfig);
    }

    DatabaseTableConfig<PlayerMuteRecord> playerMuteRecordsConfig = (DatabaseTableConfig<PlayerMuteRecord>) configuration
            .getLocalDb().getTable("playerMuteRecords");
    playerMuteRecordStorage = new PlayerMuteRecordStorage(localConn, playerMuteRecordsConfig);

    if (!playerMuteRecordStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerMuteRecordsConfig);
    }

    DatabaseTableConfig<PlayerWarnData> playerWarningsConfig = (DatabaseTableConfig<PlayerWarnData>) configuration
            .getLocalDb().getTable("playerWarnings");
    playerWarnStorage = new PlayerWarnStorage(localConn, playerWarningsConfig);

    if (!playerWarnStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerWarningsConfig);
    }

    DatabaseTableConfig<PlayerKickData> playerKickConfig = (DatabaseTableConfig<PlayerKickData>) configuration
            .getLocalDb().getTable("playerKicks");
    playerKickStorage = new PlayerKickStorage(localConn, playerKickConfig);

    if (!playerKickStorage.isTableExists()) {
      TableUtils.createTable(localConn, playerKickConfig);
    }

    DatabaseTableConfig<IpBanData> ipBansConfig = (DatabaseTableConfig<IpBanData>) configuration.getLocalDb()
                                                                                                .getTable("ipBans");
    ipBanStorage = new IpBanStorage(localConn, ipBansConfig);

    if (!ipBanStorage.isTableExists()) {
      TableUtils.createTable(localConn, ipBansConfig);
    }

    DatabaseTableConfig<IpBanRecord> ipBanRecordsConfig = (DatabaseTableConfig<IpBanRecord>) configuration.getLocalDb()
                                                                                                          .getTable("ipBanRecords");
    ipBanRecordStorage = new IpBanRecordStorage(localConn, ipBanRecordsConfig);

    if (!ipBanRecordStorage.isTableExists()) {
      TableUtils.createTable(localConn, ipBanRecordsConfig);
    }

    if (externalConn == null) {
      return;
    }

    DatabaseTableConfig<ExternalPlayerBanData> externalPlayerBanConfig = (DatabaseTableConfig<
            ExternalPlayerBanData>) configuration.getExternalDb().getTable("playerBans");
    externalPlayerBanStorage = new ExternalPlayerBanStorage(externalConn, externalPlayerBanConfig);

    if (!externalPlayerBanStorage.isTableExists()) {
      TableUtils.createTable(externalConn, externalPlayerBanConfig);
    }

    DatabaseTableConfig<ExternalPlayerBanRecordData> externalPlayerBanRecordConfig = (DatabaseTableConfig<
            ExternalPlayerBanRecordData>) configuration.getExternalDb().getTable("playerUnbans");
    externalPlayerBanRecordStorage = new ExternalPlayerBanRecordStorage(externalConn, externalPlayerBanRecordConfig);

    if (!externalPlayerBanRecordStorage.isTableExists()) {
      TableUtils.createTable(externalConn, externalPlayerBanRecordConfig);
    }

    DatabaseTableConfig<ExternalPlayerMuteData> externalPlayerMuteConfig = (DatabaseTableConfig<
            ExternalPlayerMuteData>) configuration.getExternalDb().getTable("playerMutes");
    externalPlayerMuteStorage = new ExternalPlayerMuteStorage(externalConn, externalPlayerMuteConfig);

    if (!externalPlayerMuteStorage.isTableExists()) {
      TableUtils.createTable(externalConn, externalPlayerMuteConfig);
    }

    DatabaseTableConfig<ExternalPlayerMuteRecordData> externalPlayerMuteRecordConfig = (DatabaseTableConfig<
            ExternalPlayerMuteRecordData>) configuration.getExternalDb().getTable("playerUnmutes");
    externalPlayerMuteRecordStorage = new ExternalPlayerMuteRecordStorage(externalConn, externalPlayerMuteRecordConfig);

    if (!externalPlayerMuteRecordStorage.isTableExists()) {
      TableUtils.createTable(externalConn, externalPlayerMuteRecordConfig);
    }

    DatabaseTableConfig<ExternalIpBanData> externalIpBanConfig = (DatabaseTableConfig<
            ExternalIpBanData>) configuration.getExternalDb().getTable("ipBans");
    externalIpBanStorage = new ExternalIpBanStorage(externalConn, externalIpBanConfig);

    if (!externalIpBanRecordStorage.isTableExists()) {
      TableUtils.createTable(externalConn, externalIpBanConfig);
    }

    DatabaseTableConfig<ExternalIpBanRecordData> externalIpBanRecordConfig = (DatabaseTableConfig<
            ExternalIpBanRecordData>) configuration.getExternalDb().getTable("ipUnbans");
    externalIpBanRecordStorage = new ExternalIpBanRecordStorage(externalConn, externalIpBanRecordConfig);

    if (!externalIpBanRecordStorage.isTableExists()) {
      TableUtils.createTable(externalConn, externalIpBanRecordConfig);
    }
  }

  @Override
  public void setupListeners() {
    new JoinListener().register();
    new LeaveListener().register();
    new ChatListener().register();
    new CommandListener().register();

    if (configuration.isDisplayNotificationsEnabled()) {
      new BanListener().register();
      new MuteListener().register();
    }
  }

  @Override
  public void setupRunnables() {
    setupAsyncRunnable(schedulesConfig.getSchedule("playerBans"), new BanSync());
    setupAsyncRunnable(schedulesConfig.getSchedule("playerMutes"), new MuteSync());
    setupAsyncRunnable(schedulesConfig.getSchedule("ipBans"), new IpSync());
    setupAsyncRunnable(schedulesConfig.getSchedule("expiresCheck"), new ExpiresSync());

    if (externalConn != null) {
      setupAsyncRunnable(schedulesConfig.getSchedule("externalPlayerBans"), new ExternalBanSync());
      setupAsyncRunnable(schedulesConfig.getSchedule("externalPlayerMutes"), new ExternalMuteSync());
      setupAsyncRunnable(schedulesConfig.getSchedule("externalIpBans"), new ExternalIpSync());
    }

    /*
     * This task should be ran last with a 1L offset as it gets modified
     * above.
     */
    setupAsyncRunnable(schedulesConfig.getSchedule("saveLastChecked") + 1L, new SaveLastChecked());
  }

  private void setupAsyncRunnable(long length, Runnable runnable) {
    if (length <= 0) return;

    getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, length, length);
  }
}
