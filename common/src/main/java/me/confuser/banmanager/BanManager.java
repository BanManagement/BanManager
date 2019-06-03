package me.confuser.banmanager;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.confuser.banmanager.common.commands.*;
import me.confuser.banmanager.common.commands.global.*;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.configs.*;
import me.confuser.banmanager.runnables.*;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.storage.global.*;
import me.confuser.banmanager.storage.mariadb.MariaDBDatabase;
import me.confuser.banmanager.storage.mysql.ConvertMyISAMToInnoDb;
import me.confuser.banmanager.storage.mysql.MySQLDatabase;
import me.confuser.banmanager.util.DateUtils;

import java.io.IOException;
import java.sql.SQLException;

public class BanManager {

  @Getter
  public static BanManagerPlugin plugin;
  @Getter
  private ConnectionSource localConn;
  private ConnectionSource globalConn;

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
  private DefaultConfig configuration;
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
  private Runner syncRunner;

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
      getPlugin().getBootstrap().getPluginLogger().warn("An error occurred attempting to make a database connection, please see stack trace below");
      plugin.getPluginLoader().disablePlugin(this);
      e.printStackTrace();
      return;
    }

    try {
      long timeDiff = DateUtils.findTimeDiff();

      if (timeDiff > 1) {
        getPlugin().getBootstrap().getPluginLogger().severe("The time on your server and MySQL database are out by " + timeDiff + " seconds, this may cause syncing issues.");
      }
    } catch (SQLException | IOException e) {
      getPlugin().getBootstrap().getPluginLogger().warn("An error occurred attempting to find the time difference, please see stack trace below");
      plugin.getPluginLoader().disablePlugin(this);
      e.printStackTrace();
    }
    setupListeners();
    setupCommands();
    setupRunnables();
  }

  @Override
  public void onDisable() {
    getPlugin().getBootstrap().getScheduler().shutdown();

    if (localConn != null) {
      // Save all player histories
      if (configuration.isLogIpsEnabled() && playerHistoryStorage != null) {
        playerHistoryStorage.save();
      }

      localConn.closeQuietly();
    }

    if (globalConn != null) {
      globalConn.closeQuietly();
    }

  }

  private void disableDatabaseLogging() {
    System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
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
    new BanIpRangeCommand().register();
    new TempIpRangeBanCommand().register();
    new UnbanIpRangeCommand().register();

    // IP Mutes
    new MuteIpCommand().register();
    new TempIpMuteCommand().register();
    new UnmuteIpCommand().register();

    // Misc
    new ExportCommand().register();
    new ImportCommand().register();
    new FindAltsCommand().register();
    new ReloadCommand().register();
    new InfoCommand().register();
    new BanListCommand().register();
    new ActivityCommand().register();

    // Reports
    new ReportCommand().register();
    new ReportsCommand().register();

    // Kicks
    new KickCommand().register();
    new LoglessKickCommand().register();

    new BanNameCommand().register();
    new TempNameBanCommand().register();
    new UnbanNameCommand().register();

    new WarnCommand().register();
    new TempWarnCommand().register();
    new DeleteLastWarningCommand().register();

    new AddNoteCommand().register();
    new NotesCommand().register();

    new ClearCommand().register();
    new DeleteCommand().register();
    new RollbackCommand().register();

    new SyncCommand().register();

    new ReasonsCommand().register();

    new UtilsCommand().register();

    if (globalConn == null) return;

    new BanAllCommand().register();
    new TempBanAllCommand().register();
    new UnbanAllCommand().register();

    new MuteAllCommand().register();
    new TempMuteAllCommand().register();
    new UnmuteAllCommand().register();

    new BanIpAllCommand().register();
    new TempBanIpAllCommand().register();
    new UnbanIpAllCommand().register();

    new AddNoteAllCommand().register();
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

    exemptionsConfig = new ExemptionsConfig();
    exemptionsConfig.load();

    reasonsConfig = new ReasonsConfig();
    reasonsConfig.load();

    geoIpConfig = new GeoIpConfig();
    geoIpConfig.load();
  }

  public boolean setupConnections() throws SQLException {
    if (!configuration.getLocalDb().isEnabled()) {
      getPlugin().getBootstrap().getPluginLogger().warn("Local Database is not enabled, disabling plugin");
      plugin.getPluginLoader().disablePlugin(this);
      return false;
    }

    localConn = setupConnection(configuration.getLocalDb(), "bm-local");

    if (configuration.getGlobalDb().isEnabled()) {
      globalConn = setupConnection(configuration.getGlobalDb(), "bm-global");
    }

    return true;
  }

  private ConnectionSource setupConnection(DatabaseConfig dbConfig, String type) throws SQLException {
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

  @SuppressWarnings("unchecked")
  public void setupStorages() throws SQLException {
    // TODO Refactor this
    new ConvertMyISAMToInnoDb(localConn, getConfiguration().getLocalDb().getTables()); // Convert to InnoDb if MyISAM

    playerStorage = new PlayerStorage(localConn);
    playerBanStorage = new PlayerBanStorage(localConn);
    playerBanRecordStorage = new PlayerBanRecordStorage(localConn);
    playerMuteStorage = new PlayerMuteStorage(localConn);
    playerMuteRecordStorage = new PlayerMuteRecordStorage(localConn);
    playerWarnStorage = new PlayerWarnStorage(localConn);
    playerKickStorage = new PlayerKickStorage(localConn);
    playerNoteStorage = new PlayerNoteStorage(localConn);
    playerHistoryStorage = new PlayerHistoryStorage(localConn);
    reportStateStorage = new ReportStateStorage(localConn);
    playerReportCommandStorage = new PlayerReportCommandStorage(localConn);
    playerReportCommentStorage = new PlayerReportCommentStorage(localConn);
    playerReportStorage = new PlayerReportStorage(localConn);
    playerReportLocationStorage = new PlayerReportLocationStorage(localConn);

    ipBanStorage = new IpBanStorage(localConn);
    ipBanRecordStorage = new IpBanRecordStorage(localConn);
    ipMuteStorage = new IpMuteStorage(localConn);
    ipMuteRecordStorage = new IpMuteRecordStorage(localConn);
    ipRangeBanStorage = new IpRangeBanStorage(localConn);
    ipRangeBanRecordStorage = new IpRangeBanRecordStorage(localConn);

    activityStorage = new ActivityStorage(localConn);
    historyStorage = new HistoryStorage(localConn);
    rollbackStorage = new RollbackStorage(localConn);

    nameBanStorage = new NameBanStorage(localConn);
    nameBanRecordStorage = new NameBanRecordStorage(localConn);

    if (globalConn == null) {
      return;
    }

    new ConvertMyISAMToInnoDb(globalConn, getConfiguration().getGlobalDb().getTables()); // Convert to InnoDb if MyISAM

    globalPlayerBanStorage = new GlobalPlayerBanStorage(globalConn);
    globalPlayerBanRecordStorage = new GlobalPlayerBanRecordStorage(globalConn);
    globalPlayerMuteStorage = new GlobalPlayerMuteStorage(globalConn);
    globalPlayerMuteRecordStorage = new GlobalPlayerMuteRecordStorage(globalConn);
    globalPlayerNoteStorage = new GlobalPlayerNoteStorage(globalConn);
    globalIpBanStorage = new GlobalIpBanStorage(globalConn);
    globalIpBanRecordStorage = new GlobalIpBanRecordStorage(globalConn);
  }

}
