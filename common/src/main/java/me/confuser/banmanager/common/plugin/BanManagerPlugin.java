package me.confuser.banmanager.common.plugin;

import me.confuser.banmanager.common.command.CommandManager;
import me.confuser.banmanager.common.config.BanManagerConfiguration;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.plugin.bootstrap.BanManagerBootstrap;
import me.confuser.banmanager.common.plugin.logging.PluginLogger;
import me.confuser.banmanager.common.sender.SenderFactory;
import me.confuser.banmanager.runnables.Runner;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.storage.global.*;

public interface BanManagerPlugin {

    /**
     * Gets the bootstrap plugin instance
     *
     * @return the bootstrap plugin
     */
    BanManagerBootstrap getBootstrap();

    /**
     * Gets a wrapped logger instance for the platform.
     *
     * @return the plugin's logger
     */
    PluginLogger getLogger();

    /**
     * Gets the plugin's configuration
     *
     * @return the plugin config
     */
    BanManagerConfiguration getConfiguration();

    /**
     * Gets the instance providing locale translations for the plugin
     *
     * @return the locale manager
     */
    LocaleManager getLocaleManager();

    /**
     * Gets the command manager
     *
     * @return the command manager
     */
    CommandManager getCommandManager();

    SenderFactory<?> getSenderFactory();

    PlayerStorage getPlayerStorage();
    HistoryStorage getHistoryStorage();
    PlayerBanRecordStorage getPlayerBanRecordStorage();
    PlayerMuteRecordStorage getPlayerMuteRecordStorage();
    PlayerWarnStorage getPlayerWarnStorage();
    PlayerKickStorage getPlayerKickStorage();
    IpBanRecordStorage getIpBanRecordStorage();
    PlayerBanStorage getPlayerBanStorage();
    PlayerMuteStorage getPlayerMuteStorage();
    PlayerHistoryStorage getPlayerHistoryStorage();
    IpBanStorage getIpBanStorage();
    ActivityStorage getActivityStorage();
    GlobalPlayerBanStorage getGlobalPlayerBanStorage();
    GlobalPlayerNoteStorage getGlobalPlayerNoteStorage();
    PlayerNoteStorage getPlayerNoteStorage();
    IpRangeBanStorage getIpRangeBanStorage();
    GlobalIpBanStorage getGlobalIpBanStorage();
    GlobalPlayerMuteStorage getGlobalPlayerMuteStorage();
    IpMuteStorage getIpMuteStorage();
    IpMuteRecordStorage getIpMuteRecordStorage();
    RollbackStorage getRollbackStorage();
    NameBanStorage getNameBanStorage();
    NameBanRecordStorage getNameBanRecordStorage();
    IpRangeBanRecordStorage getIpRangeBanRecordStorage();
    GlobalPlayerMuteRecordStorage getGlobalPlayerMuteRecordStorage();
    GlobalIpBanRecordStorage getGlobalIpBanRecordStorage();
    GlobalPlayerBanRecordStorage getGlobalPlayerBanRecordStorage();
    PlayerReportStorage getPlayerReportStorage();
    ReportStateStorage getReportStateStorage();
    PlayerReportCommandStorage getPlayerReportCommandStorage();
    PlayerReportCommentStorage getPlayerReportCommentStorage();
    PlayerReportLocationStorage getPlayerReportLocationStorage();

    Runner getSyncRunner();


}
