package me.confuser.banmanager.common;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.db.H2DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;
import me.confuser.banmanager.common.api.BmAPI;
import me.confuser.banmanager.common.commands.*;
import me.confuser.banmanager.common.commands.global.*;
import me.confuser.banmanager.common.configs.*;
import me.confuser.banmanager.common.runnables.Runner;
import me.confuser.banmanager.common.storage.*;
import me.confuser.banmanager.common.storage.global.*;
import me.confuser.banmanager.common.storage.mariadb.MariaDBDatabase;
import me.confuser.banmanager.common.storage.mysql.MySQLDatabase;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import static java.lang.Long.parseLong;

public class BanManagerPlugin {
  private static BanManagerPlugin self;

  /*
   * This block prevents the Maven Shade plugin to remove the specified classes
   */
  static {
    @SuppressWarnings("unused") Class<?>[] classes = new Class<?>[]{
        BmAPI.class,
    };
  }

  @Getter
  private PluginInfo pluginInfo;
  @Getter
  private final CommonLogger logger;
  @Getter
  private final CommonMetrics metrics;

  // Configs
  @Getter
  private File dataFolder;
  @Getter
  private DefaultConfig config;
  @Getter
  private ConsoleConfig consoleConfig;
  @Getter
  private SchedulesConfig schedulesConfig;
  @Getter
  private ExemptionsConfig exemptionsConfig;
  @Getter
  private ReasonsConfig reasonsConfig;
  @Getter
  private GeoIpConfig geoIpConfig;
  @Getter
  private DiscordConfig discordConfig;

  // Connections
  @Getter
  private ConnectionSource localConn;
  @Getter
  private ConnectionSource globalConn;

  // Storage
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
  private PlayerNoteStorage playerNoteStorage;
  @Getter
  private ActivityStorage activityStorage;
  @Getter
  private HistoryStorage historyStorage;
  @Getter
  private PlayerHistoryStorage playerHistoryStorage;
  @Getter
  private PlayerReportStorage playerReportStorage;
  @Getter
  private PlayerReportLocationStorage playerReportLocationStorage;
  @Getter
  private ReportStateStorage reportStateStorage;
  @Getter
  private PlayerReportCommandStorage playerReportCommandStorage;
  @Getter
  private PlayerReportCommentStorage playerReportCommentStorage;
  @Getter
  private RollbackStorage rollbackStorage;

  @Getter
  private NameBanStorage nameBanStorage;
  @Getter
  private NameBanRecordStorage nameBanRecordStorage;

  @Getter
  private IpBanStorage ipBanStorage;
  @Getter
  private IpBanRecordStorage ipBanRecordStorage;
  @Getter
  private IpMuteStorage ipMuteStorage;
  @Getter
  private IpMuteRecordStorage ipMuteRecordStorage;
  @Getter
  private IpRangeBanStorage ipRangeBanStorage;
  @Getter
  private IpRangeBanRecordStorage ipRangeBanRecordStorage;

  @Getter
  private GlobalPlayerBanStorage globalPlayerBanStorage;
  @Getter
  private GlobalPlayerBanRecordStorage globalPlayerBanRecordStorage;
  @Getter
  private GlobalPlayerMuteStorage globalPlayerMuteStorage;
  @Getter
  private GlobalPlayerMuteRecordStorage globalPlayerMuteRecordStorage;
  @Getter
  private GlobalPlayerNoteStorage globalPlayerNoteStorage;

  @Getter
  private GlobalIpBanStorage globalIpBanStorage;
  @Getter
  private GlobalIpBanRecordStorage globalIpBanRecordStorage;

  @Getter
  private CommonServer server;
  @Getter
  private CommonScheduler scheduler;
  @Setter
  @Getter
  private Runner syncRunner;

  public BanManagerPlugin(PluginInfo pluginInfo, CommonLogger logger, File dataFolder, CommonScheduler scheduler, CommonServer server, CommonMetrics metrics) {
    this.pluginInfo = pluginInfo;
    this.logger = logger;
    this.dataFolder = dataFolder;
    this.server = server;
    this.scheduler = scheduler;
    this.metrics = metrics;
    self = this;
  }

  public final void enable() throws Exception {
    setupConfigs();

    try {
      if (!config.isDebugEnabled()) {
        disableDatabaseLogging();
      }

      if (!setupConnections()) {
        throw new Exception("Unable to connect to database, ensure local is enabled in config and your connection details are correct");
      }

      setupStorage();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new Exception("An error occurred attempting to make a database connection, please see stack trace below");
    }

    String query = "SELECT UNIX_TIMESTAMP() - ? as mysqlTime";

    GenericRawResults<String[]> results = playerStorage
        .queryRaw(query, String.valueOf(System.currentTimeMillis() / 1000L));

    String result = results.getFirstResult()[0];

    results.close();

    long timeDiff;

    // Some drivers appear to return a decimal such as MariaDB e.g. 0.0
    if (result.contains(".")) {
      timeDiff = Double.valueOf(result).longValue();
    } else {
      timeDiff = parseLong(result);
    }

    results.close();

    if (timeDiff > 1) {
      logger
          .severe("The time on your server and MySQL database are out by " + timeDiff + " seconds, this may cause syncing issues.");
    }

    String storageVersion = null;
    if (!config.getLocalDb().getStorageType().equals("h2")) {
      // Get database version
      GenericRawResults<String[]> results2 = playerStorage
          .queryRaw("SELECT VERSION()");
      
      storageVersion = results2.getFirstResult()[0];

      results2.close();
    }

    try {
      metrics.submitStorageType(config.getLocalDb().getStorageType());
      metrics.submitDiscordMode(discordConfig.isEnabled());
      metrics.submitGeoMode(geoIpConfig.isEnabled());
      metrics.submitGlobalMode(config.getGlobalDb().isEnabled());
      metrics.submitOnlineMode(config.isOnlineMode());
      if (storageVersion != null) {
        metrics.submitStorageVersion(storageVersion);
      }
    } catch (Exception e) {
      logger.warning("Failed to submit stats, ignoring");
    }
  }

  public final void disable() {
    if (getSchedulesConfig() != null) {
      getSchedulesConfig().save();
    }

    if (localConn != null) {
      // Save all player histories
      if (config.isLogIpsEnabled() && playerHistoryStorage != null) {
        playerHistoryStorage.save();
      }

      localConn.closeQuietly();
    }

    if (globalConn != null) {
      globalConn.closeQuietly();
    }
  }

  public void setupConfigs() {
    new MessagesConfig(dataFolder, logger).load();

    config = new DefaultConfig(dataFolder, logger);
    config.load();

    consoleConfig = new ConsoleConfig(dataFolder, logger);
    consoleConfig.load();

    schedulesConfig = new SchedulesConfig(dataFolder, logger);
    schedulesConfig.load();

    exemptionsConfig = new ExemptionsConfig(dataFolder, logger);
    exemptionsConfig.load();

    reasonsConfig = new ReasonsConfig(dataFolder, logger);
    reasonsConfig.load();

    geoIpConfig = new GeoIpConfig(dataFolder, logger);
    geoIpConfig.load();

    discordConfig = new DiscordConfig(dataFolder, logger);
    discordConfig.load();
  }

  private void disableDatabaseLogging() {
    System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "WARNING");
  }

  public boolean setupConnections() throws SQLException {
    if (!config.getLocalDb().isEnabled()) {
      getLogger().warning("Local Database is not enabled, disabling plugin");
      return false;
    }

    localConn = createConnection(config.getLocalDb(), "bm-local");

    if (config.getGlobalDb().isEnabled()) {
      globalConn = createConnection(config.getGlobalDb(), "bm-global");
    }

    return true;
  }

  public ConnectionSource createConnection(DatabaseConfig dbConfig, String type) throws SQLException {
    HikariDataSource ds = new HikariDataSource();

    if (!dbConfig.getStorageType().equals("h2")) {
      if (!dbConfig.getUser().isEmpty()) {
        ds.setUsername(dbConfig.getUser());
      }
      if (!dbConfig.getPassword().isEmpty()) {
        ds.setPassword(dbConfig.getPassword());
      }
    }

    ds.setJdbcUrl(dbConfig.getJDBCUrl());
    ds.setMaximumPoolSize(dbConfig.getMaxConnections());
    ds.setMinimumIdle(2);
    ds.setPoolName(type);
    ds.setConnectionTimeout(dbConfig.getConnectionTimeout());
    ds.setMaxLifetime(dbConfig.getMaxLifetime());

    if (dbConfig.getLeakDetection() != 0) ds.setLeakDetectionThreshold(dbConfig.getLeakDetection());

    DatabaseType databaseType;

    if (dbConfig.getStorageType().equals("mariadb")) {
      databaseType = new MariaDBDatabase();
    } else if (dbConfig.getStorageType().equals("mysql")) {
      // Forcefully specify the newer driver
      ds.setDriverClassName("com.mysql.cj.jdbc.Driver");

      databaseType = new MySQLDatabase();

      ds.addDataSourceProperty("cachePrepStmts", "true");
      ds.addDataSourceProperty("prepStmtCacheSize", "250");
      ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      ds.addDataSourceProperty("useServerPrepStmts", "true");
      ds.addDataSourceProperty("useLocalSessionState", "true");
      ds.addDataSourceProperty("rewriteBatchedStatements", "true");
      ds.addDataSourceProperty("cacheResultSetMetadata", "true");
      ds.addDataSourceProperty("cacheServerConfiguration", "true");
      ds.addDataSourceProperty("elideSetAutoCommits", "true");
      ds.addDataSourceProperty("maintainTimeStats", "false");
      ds.addDataSourceProperty("alwaysSendSetIsolation", "false");
      ds.addDataSourceProperty("cacheCallableStmts", "true");
    } else {
      try {
        // Force usage of BM H2 in server implementations with differing versions
        ds.setDriverClassName("me.confuser.banmanager.common.h2.Driver");
      } catch (RuntimeException e) {
        // Required for integration tests
        ds.setDriverClassName("org.h2.Driver");
      }

      databaseType = new H2DatabaseType();
    }

    return new DataSourceConnectionSource(ds, databaseType);
  }

  public void setupStorage() throws SQLException {
    // Setup h2 specific functions
    if (config.getLocalDb().getStorageType().equals("h2")) {
      try (DatabaseConnection conn = getLocalConn().getReadWriteConnection("")) {
        conn.executeStatement("CREATE ALIAS IF NOT EXISTS INET6_NTOA FOR \"me.confuser.banmanager.common.util.IPUtils.toString\"", DatabaseConnection.DEFAULT_RESULT_FLAGS);
      } catch (IOException | SQLException e) {
        e.printStackTrace();
      }
    }

    playerStorage = new PlayerStorage(this);
    playerBanStorage = new PlayerBanStorage(this);
    playerBanRecordStorage = new PlayerBanRecordStorage(this);
    playerMuteStorage = new PlayerMuteStorage(this);
    playerMuteRecordStorage = new PlayerMuteRecordStorage(this);
    playerWarnStorage = new PlayerWarnStorage(this);
    playerKickStorage = new PlayerKickStorage(this);
    playerNoteStorage = new PlayerNoteStorage(this);
    playerHistoryStorage = new PlayerHistoryStorage(this);
    reportStateStorage = new ReportStateStorage(this);
    playerReportCommandStorage = new PlayerReportCommandStorage(this);
    playerReportCommentStorage = new PlayerReportCommentStorage(this);
    playerReportStorage = new PlayerReportStorage(this);
    playerReportLocationStorage = new PlayerReportLocationStorage(this);

    ipBanStorage = new IpBanStorage(this);
    ipBanRecordStorage = new IpBanRecordStorage(this);
    ipMuteStorage = new IpMuteStorage(this);
    ipMuteRecordStorage = new IpMuteRecordStorage(this);
    ipRangeBanStorage = new IpRangeBanStorage(this);
    ipRangeBanRecordStorage = new IpRangeBanRecordStorage(this);

    activityStorage = new ActivityStorage(this);
    historyStorage = new HistoryStorage(this);
    rollbackStorage = new RollbackStorage(this);

    nameBanStorage = new NameBanStorage(this);
    nameBanRecordStorage = new NameBanRecordStorage(this);

    if (globalConn == null) {
      return;
    }

    globalPlayerBanStorage = new GlobalPlayerBanStorage(this);
    globalPlayerBanRecordStorage = new GlobalPlayerBanRecordStorage(this);
    globalPlayerMuteStorage = new GlobalPlayerMuteStorage(this);
    globalPlayerMuteRecordStorage = new GlobalPlayerMuteRecordStorage(this);
    globalPlayerNoteStorage = new GlobalPlayerNoteStorage(this);
    globalIpBanStorage = new GlobalIpBanStorage(this);
    globalIpBanRecordStorage = new GlobalIpBanRecordStorage(this);
  }

  public CommonCommand[] getCommands() {
    return new CommonCommand[]{
        new ActivityCommand(this),
        new AddNoteCommand(this),
        new BanCommand(this),
        new BanIpCommand(this),
        new BanIpRangeCommand(this),
        new BanListCommand(this),
        new BanNameCommand(this),
        new ClearCommand(this),
        new DeleteCommand(this),
        new DeleteLastWarningCommand(this),
        new ExportCommand(this),
        new FindAltsCommand(this),
        new InfoCommand(this),
        new ImportCommand(this),
        new KickCommand(this),
        new LoglessKickCommand(this),
        new MuteCommand(this),
        new MuteIpCommand(this),
        new NotesCommand(this),
        new ReasonsCommand(this),
        new ReloadCommand(this),
        new ReportCommand(this),
        new ReportsCommand(this),
        new RollbackCommand(this),
        new SyncCommand(this),
        new TempBanCommand(this),
        new TempIpBanCommand(this),
        new TempIpMuteCommand(this),
        new TempIpRangeBanCommand(this),
        new TempMuteCommand(this),
        new TempNameBanCommand(this),
        new TempWarnCommand(this),
        new UnbanCommand(this),
        new UnbanIpCommand(this),
        new UnbanIpRangeCommand(this),
        new UnbanNameCommand(this),
        new UnmuteCommand(this),
        new UnmuteIpCommand(this),
        new UtilsCommand(this),
        new WarnCommand(this)
    };
  }

  public CommonCommand[] getGlobalCommands() {
    return new CommonCommand[]{
        new AddNoteAllCommand(this),
        new BanAllCommand(this),
        new BanIpAllCommand(this),
        new MuteAllCommand(this),
        new TempBanAllCommand(this),
        new TempBanIpAllCommand(this),
        new TempMuteAllCommand(this),
        new UnbanAllCommand(this),
        new UnbanIpAllCommand(this),
        new UnmuteAllCommand(this)
    };
  }

  public static BanManagerPlugin getInstance() {
    return self;
  }
}
