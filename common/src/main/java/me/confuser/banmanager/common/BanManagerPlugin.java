package me.confuser.banmanager.common;

import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.confuser.banmanager.common.configs.*;
import me.confuser.banmanager.common.storage.*;
import me.confuser.banmanager.common.storage.global.*;
import me.confuser.banmanager.common.storage.mariadb.MariaDBDatabase;
import me.confuser.banmanager.common.storage.mysql.MySQLDatabase;

import java.io.File;
import java.sql.SQLException;

import static java.lang.Long.parseLong;

public class BanManagerPlugin {

  @Getter
  private final CommonLogger logger;

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

  public BanManagerPlugin(CommonLogger logger, File dataFolder) {
    this.logger = logger;
    this.dataFolder = dataFolder;
  }

  public final void enable() throws Exception {
    setupConfigs();

    try {
      if (!config.isDebugEnabled()) {
        disableDatabaseLogging();
      }

      if (!setupConnections()) {
        return;
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
  }

  public final void disable() {
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
  }

  private void disableDatabaseLogging() {
    System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
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

  private ConnectionSource createConnection(DatabaseConfig dbConfig, String type) throws SQLException {
    HikariDataSource ds = new HikariDataSource();

    if (!dbConfig.getUser().isEmpty()) {
      ds.setUsername(dbConfig.getUser());
    }
    if (!dbConfig.getPassword().isEmpty()) {
      ds.setPassword(dbConfig.getPassword());
    }

    ds.setJdbcUrl(dbConfig.getJDBCUrl());
    ds.setMaximumPoolSize(dbConfig.getMaxConnections());
    ds.setMinimumIdle(2);
    ds.setPoolName(type);

    if (dbConfig.getLeakDetection() != 0) ds.setLeakDetectionThreshold(dbConfig.getLeakDetection());

    DatabaseType databaseType;

    if (dbConfig.getStorageType().equals("mariadb")) {
      databaseType = new MariaDBDatabase();
    } else {
      // Forcefully specify the newer driver
      ds.setDriverClassName("com.mysql.cj.jdbc.Driver");

      databaseType = new MySQLDatabase();

      ds.addDataSourceProperty("useServerPrepStmts", "true");
      ds.addDataSourceProperty("prepStmtCacheSize", "250");
      ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
      ds.addDataSourceProperty("cachePrepStmts", "true");
    }

    return new DataSourceConnectionSource(ds, databaseType);
  }

  public void setupStorage() throws SQLException {
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
}
