package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.storage.*;
import me.confuser.banmanager.common.util.DateUtils;

import java.sql.SQLException;

public class ExpiresSync extends BmRunnable {

  private PlayerBanStorage banStorage;
  private PlayerBanRecordStorage banRecordStorage;
  private PlayerMuteStorage muteStorage;
  private PlayerMuteRecordStorage muteRecordStorage;
  private IpBanStorage ipBanStorage;
  private IpBanRecordStorage ipBanRecordStorage;
  private IpMuteStorage ipMuteStorage;
  private IpMuteRecordStorage ipMuteRecordStorage;
  private IpRangeBanStorage ipRangeBanStorage;
  private IpRangeBanRecordStorage ipRangeBanRecordStorage;
  private PlayerWarnStorage warnStorage;

  public ExpiresSync(BanManagerPlugin plugin) {
    super(plugin, "expiresCheck");

    banStorage = plugin.getPlayerBanStorage();
    banRecordStorage = plugin.getPlayerBanRecordStorage();
    muteStorage = plugin.getPlayerMuteStorage();
    muteRecordStorage = plugin.getPlayerMuteRecordStorage();
    ipBanStorage = plugin.getIpBanStorage();
    ipBanRecordStorage = plugin.getIpBanRecordStorage();
    ipMuteStorage = plugin.getIpMuteStorage();
    ipMuteRecordStorage = plugin.getIpMuteRecordStorage();
    ipRangeBanStorage = plugin.getIpRangeBanStorage();
    ipRangeBanRecordStorage = plugin.getIpRangeBanRecordStorage();
    warnStorage = plugin.getPlayerWarnStorage();
  }

  @Override
  public void run() {
    long now = (System.currentTimeMillis() / 1000L) + DateUtils.getTimeDiff();

    CloseableIterator<PlayerBanData> bans = null;
    try {
      bans = banStorage.queryBuilder().where().ne("expires", 0).and()
          .le("expires", now).iterator();

      while (bans.hasNext()) {
        PlayerBanData ban = bans.next();

        banStorage.unban(ban, plugin.getPlayerStorage().getConsole(), "");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (bans != null) bans.closeQuietly();
    }

    CloseableIterator<PlayerMuteData> mutes = null;
    try {
      mutes = muteStorage.queryBuilder().where().ne("expires", 0).and().le("expires", now).iterator();

      while (mutes.hasNext()) {
        PlayerMuteData mute = mutes.next();

        muteStorage.unmuteIfExpired(mute, plugin.getPlayerStorage().getConsole());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (mutes != null) mutes.closeQuietly();
    }

    CloseableIterator<PlayerWarnData> warnings = null;
    try {
      warnings = warnStorage.queryBuilder().where().ne("expires", 0).and()
          .le("expires", now).iterator();

      while (warnings.hasNext()) {
        PlayerWarnData warning = warnings.next();

        warnStorage.delete(warning);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (warnings != null) warnings.closeQuietly();
    }

    CloseableIterator<IpBanData> ipBans = null;
    try {
      ipBans = ipBanStorage.queryBuilder().where().ne("expires", 0).and()
          .le("expires", now).iterator();

      while (ipBans.hasNext()) {
        IpBanData ban = ipBans.next();

        ipBanStorage.unban(ban, plugin.getPlayerStorage().getConsole(), "");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (ipBans != null) ipBans.closeQuietly();
    }

    CloseableIterator<IpMuteData> ipMutes = null;
    try {
      ipMutes = ipMuteStorage.queryBuilder().where().ne("expires", 0).and().le("expires", now).iterator();

      while (ipMutes.hasNext()) {
        IpMuteData mute = ipMutes.next();

        ipMuteStorage.unmute(mute, plugin.getPlayerStorage().getConsole(), "");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (ipMutes != null) ipMutes.closeQuietly();
    }

    CloseableIterator<IpRangeBanData> ipRangeBans = null;
    try {
      ipRangeBans = ipRangeBanStorage.queryBuilder().where().ne("expires", 0).and()
          .le("expires", now).iterator();

      while (ipRangeBans.hasNext()) {
        IpRangeBanData ban = ipRangeBans.next();

        ipRangeBanStorage.unban(ban, plugin.getPlayerStorage().getConsole(), "");
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (ipRangeBans != null) ipRangeBans.closeQuietly();
    }
  }
}
