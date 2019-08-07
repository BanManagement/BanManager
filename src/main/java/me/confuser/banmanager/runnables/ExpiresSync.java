package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.storage.*;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExpiresSync extends BmRunnable {

  private PlayerBanStorage banStorage = plugin.getPlayerBanStorage();
  private PlayerBanRecordStorage banRecordStorage = plugin.getPlayerBanRecordStorage();
  private PlayerMuteStorage muteStorage = plugin.getPlayerMuteStorage();
  private PlayerMuteRecordStorage muteRecordStorage = plugin.getPlayerMuteRecordStorage();
  private IpBanStorage ipBanStorage = plugin.getIpBanStorage();
  private IpBanRecordStorage ipBanRecordStorage = plugin.getIpBanRecordStorage();
  private IpMuteStorage ipMuteStorage = plugin.getIpMuteStorage();
  private IpMuteRecordStorage ipMuteRecordStorage = plugin.getIpMuteRecordStorage();
  private IpRangeBanStorage ipRangeBanStorage = plugin.getIpRangeBanStorage();
  private IpRangeBanRecordStorage ipRangeBanRecordStorage = plugin.getIpRangeBanRecordStorage();
  private PlayerWarnStorage warnStorage = plugin.getPlayerWarnStorage();

  public ExpiresSync() {
    super("expiresCheck");
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

        banStorage.unban(ban, plugin.getPlayerStorage().getConsole(), "", true);
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

        muteStorage.unmute(mute, plugin.getPlayerStorage().getConsole(), "", true);
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
