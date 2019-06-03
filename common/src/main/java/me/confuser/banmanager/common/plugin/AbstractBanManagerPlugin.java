package me.confuser.banmanager.common.plugin;

import com.j256.ormlite.db.DatabaseType;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import me.confuser.banmanager.common.config.AbstractConfiguration;
import me.confuser.banmanager.common.config.BanManagerConfiguration;
import me.confuser.banmanager.common.config.adapter.ConfigurationAdapter;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.plugin.logging.PluginLogger;
import me.confuser.banmanager.common.sender.SenderFactory;
import me.confuser.banmanager.configs.DatabaseConfig;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.storage.global.*;
import me.confuser.banmanager.storage.mariadb.MariaDBDatabase;
import me.confuser.banmanager.storage.mysql.ConvertMyISAMToInnoDb;
import me.confuser.banmanager.storage.mysql.MySQLDatabase;
import me.confuser.banmanager.util.DateUtils;

import java.io.IOException;
import java.sql.SQLException;

public abstract class AbstractBanManagerPlugin implements BanManagerPlugin {

    protected abstract void setupSenderFactory();
    protected abstract ConfigurationAdapter provideConfigurationAdapter();
    protected abstract void registerPlatformListeners();
    protected abstract void registerCommands();
    protected abstract void registerRunnables();

    @Getter
    private BanManagerConfiguration configuration;
    @Getter
    private LocaleManager localeManager;


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
    private ConnectionSource localConn;
    @Getter
    private ConnectionSource globalConn;


    /**
     * Performs the initial actions to load the plugin
     */
    public final void load() {
        // load dependencies
        //this.dependencyManager = new DependencyManager(this);
        //this.dependencyManager.loadDependencies(getGlobalDependencies());

        // load the sender factory instance
        setupSenderFactory();
    }

    public final void enable() {
        // https://github.com/BanManagement/BanManager/blob/master/src/main/java/me/confuser/banmanager/BanManager.java#L125
        // https://github.com/lucko/LuckPerms/blob/d3ae3324fa55bb6147abb5f9a890262d0c27cdb9/common/src/main/java/me/lucko/luckperms/common/plugin/AbstractLuckPermsPlugin.java

        // load configuration
        getLogger().info("Loading configuration...");
        this.configuration = new AbstractConfiguration(this, provideConfigurationAdapter());

        // load locale
        this.localeManager = new LocaleManager();
        this.localeManager.tryLoad(this, getBootstrap().getConfigDirectory().resolve("lang.yml"));

        try {
            if (!configuration.isDebugEnabled()) {
                disableDatabaseLogging();
            }

            if (!setupConnections()) {
                return;
            }

            setupStorages();
        } catch (SQLException e) {
            getLogger().warn("An error occurred attempting to make a database connection, please see stack trace below");
            //TODO
            //plugin.getPluginLoader().disablePlugin(this);
            e.printStackTrace();
            return;
        }

        try {
            long timeDiff = DateUtils.findTimeDiff();

            if (timeDiff > 1) {
                getLogger().severe("The time on your server and MySQL database are out by " + timeDiff + " seconds, this may cause syncing issues.");
            }
        } catch (SQLException | IOException e) {
            getLogger().warn("An error occurred attempting to find the time difference, please see stack trace below");
            //TODO
            //plugin.getPluginLoader().disablePlugin(this);
            e.printStackTrace();
        }
        registerPlatformListeners();
        registerCommands();
        registerRunnables();

    }

    public boolean setupConnections() throws SQLException {
        if (!configuration.getLocalDb().isEnabled()) {
            getLogger().warn("Local Database is not enabled, disabling plugin");
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

    @Override
    public PluginLogger getLogger() {
        return getBootstrap().getPluginLogger();
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
