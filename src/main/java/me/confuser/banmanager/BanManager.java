package me.confuser.banmanager;

import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.confuser.banmanager.commands.*;
import me.confuser.banmanager.commands.external.*;
import me.confuser.banmanager.configs.*;
import me.confuser.banmanager.listeners.*;
import me.confuser.banmanager.runnables.*;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.storage.external.*;
import me.confuser.banmanager.storage.mysql.MySQLDatabase;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UpdateUtils;
import me.confuser.bukkitutil.BukkitPlugin;
import org.mcstats.MetricsLite;

import java.io.IOException;
import java.sql.SQLException;

public class BanManager extends BukkitPlugin {

  @Getter
  public static BanManager plugin;
  @Getter
  private ConnectionSource localConn;
  private ConnectionSource externalConn;

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
  private PlayerReportStorage playerReportStorage;
  @Getter
  private PlayerReportLocationStorage playerReportLocationStorage;
  @Getter
  private ReportStateStorage reportStateStorage;

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
  private ExternalPlayerBanStorage externalPlayerBanStorage;
  @Getter
  private ExternalPlayerBanRecordStorage externalPlayerBanRecordStorage;
  @Getter
  private ExternalPlayerMuteStorage externalPlayerMuteStorage;
  @Getter
  private ExternalPlayerMuteRecordStorage externalPlayerMuteRecordStorage;
  @Getter
  private ExternalPlayerNoteStorage externalPlayerNoteStorage;

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
    getServer().getScheduler().cancelTasks(plugin);

    if (localConn != null) {
      localConn.closeQuietly();
    }

    if (externalConn != null) {
      externalConn.closeQuietly();
    }

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

    // Kicks
    new KickCommand().register();
    new LoglessKickCommand().register();

    new WarnCommand().register();
    new TempWarnCommand().register();
    new DeleteLastWarningCommand().register();

    new AddNoteCommand().register();
    new NotesCommand().register();

    new ClearCommand().register();
    new DeleteCommand().register();

    new SyncCommand().register();

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
      getLogger().warning("Local Database is not enabled, disabling plugin");
      plugin.getPluginLoader().disablePlugin(this);
      return false;
    }

    localConn = setupConnection(configuration.getLocalDb());

    if (configuration.getExternalDb().isEnabled()) {
      externalConn = setupConnection(configuration.getExternalDb());
    }

    return true;
  }

  private ConnectionSource setupConnection(DatabaseConfig dbConfig) throws SQLException {
    HikariDataSource ds = new HikariDataSource();

    if (!dbConfig.getUser().isEmpty()) {
      ds.setUsername(dbConfig.getUser());
    }
    if (!dbConfig.getPassword().isEmpty()) {
      ds.setPassword(dbConfig.getPassword());
    }

    ds.setJdbcUrl(dbConfig.getJDBCUrl());
    ds.setMaximumPoolSize(dbConfig.getMaxConnections());

    if (dbConfig.getLeakDetection() != 0) ds.setLeakDetectionThreshold(dbConfig.getLeakDetection());

    ds.addDataSourceProperty("prepStmtCacheSize", "250");
    ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
    ds.addDataSourceProperty("cachePrepStmts", "true");

    return new DataSourceConnectionSource(ds, new MySQLDatabase());
  }

  @SuppressWarnings("unchecked")
  public void setupStorages() throws SQLException {
    // TODO Refactor this
    playerStorage = new PlayerStorage(localConn);
    playerBanStorage = new PlayerBanStorage(localConn);
    playerBanRecordStorage = new PlayerBanRecordStorage(localConn);
    playerMuteStorage = new PlayerMuteStorage(localConn);
    playerMuteRecordStorage = new PlayerMuteRecordStorage(localConn);
    playerWarnStorage = new PlayerWarnStorage(localConn);
    playerKickStorage = new PlayerKickStorage(localConn);
    playerNoteStorage = new PlayerNoteStorage(localConn);
    playerReportStorage = new PlayerReportStorage(localConn);
    playerReportLocationStorage = new PlayerReportLocationStorage(localConn);
    reportStateStorage = new ReportStateStorage(localConn);

    ipBanStorage = new IpBanStorage(localConn);
    ipBanRecordStorage = new IpBanRecordStorage(localConn);
    ipMuteStorage = new IpMuteStorage(localConn);
    ipMuteRecordStorage = new IpMuteRecordStorage(localConn);
    ipRangeBanStorage = new IpRangeBanStorage(localConn);
    ipRangeBanRecordStorage = new IpRangeBanRecordStorage(localConn);

    activityStorage = new ActivityStorage(localConn);
    historyStorage = new HistoryStorage(localConn);

    if (externalConn == null) {
      return;
    }

    externalPlayerBanStorage = new ExternalPlayerBanStorage(externalConn);
    externalPlayerBanRecordStorage = new ExternalPlayerBanRecordStorage(externalConn);
    externalPlayerMuteStorage = new ExternalPlayerMuteStorage(externalConn);
    externalPlayerMuteRecordStorage = new ExternalPlayerMuteRecordStorage(externalConn);
    externalPlayerNoteStorage = new ExternalPlayerNoteStorage(externalConn);
    externalIpBanStorage = new ExternalIpBanStorage(externalConn);
    externalIpBanRecordStorage = new ExternalIpBanRecordStorage(externalConn);
  }

  @Override
  public void setupListeners() {
    new JoinListener().register();
    new ChatListener().register();
    new CommandListener().register();
    new HookListener().register();

    if (configuration.isDisplayNotificationsEnabled()) {
      new BanListener().register();
      new MuteListener().register();
      new NoteListener().register();
      new ReportListener().register();
    }
  }

  @Override
  public void setupRunnables() {
    if (externalConn == null) {
      syncRunner = new Runner(new BanSync(), new MuteSync(), new IpSync(), new IpRangeSync(), new ExpiresSync(), new WarningSync());
    } else {
      syncRunner = new Runner(new BanSync(), new MuteSync(), new IpSync(), new IpRangeSync(), new ExpiresSync(), new WarningSync(),
              new ExternalBanSync(), new ExternalMuteSync(), new ExternalIpSync(), new ExternalNoteSync());
    }

    setupAsyncRunnable(10L, syncRunner);

    /*
     * This task should be ran last with a 1L offset as it gets modified
     * above.
     */
    setupAsyncRunnable((schedulesConfig.getSchedule("saveLastChecked") * 20L) + 1L, new SaveLastChecked());

    // Purge
    getServer().getScheduler().runTaskAsynchronously(plugin, new Purge());

    // TODO Refactor
    if (!getConfiguration().isCheckForUpdates()) return;

    getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        if (UpdateUtils.isUpdateAvailable(getFile())) {
          getServer().getScheduler().runTask(plugin, new Runnable() {

            @Override
            public void run() {
              new UpdateListener().register();
            }
          });
        }
      }
    });
  }

  private void setupAsyncRunnable(long length, Runnable runnable) {
    if (length <= 0) return;

    getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, length, length);
  }
}
