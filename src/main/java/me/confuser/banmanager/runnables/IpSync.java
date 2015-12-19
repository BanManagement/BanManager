package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpBanRecord;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.IpMuteRecord;
import me.confuser.banmanager.storage.IpBanStorage;
import me.confuser.banmanager.storage.IpMuteStorage;

import java.sql.SQLException;

public class IpSync extends BmRunnable {

  private IpBanStorage banStorage = plugin.getIpBanStorage();
  private IpMuteStorage muteStorage = plugin.getIpMuteStorage();

  public IpSync() {
    super("ipBans");
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
    newMutes();
    newUnmutes();
  }

  private void newBans() {

    CloseableIterator<IpBanData> itr = null;
    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        final IpBanData ban = itr.next();

        if (banStorage.isBanned(ban.getIp()) && ban.getUpdated() < lastChecked) {
          continue;
        }

        banStorage.addBan(ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  private void newUnbans() {

    CloseableIterator<IpBanRecord> itr = null;
    try {
      itr = plugin.getIpBanRecordStorage().findUnbans(lastChecked);

      while (itr.hasNext()) {
        final IpBanRecord ban = itr.next();

        if (!banStorage.isBanned(ban.getIp())) {
          continue;
        }

        banStorage.removeBan(ban.getIp());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  private void newMutes() {

    CloseableIterator<IpMuteData> itr = null;
    try {
      itr = muteStorage.findMutes(lastChecked);

      while (itr.hasNext()) {
        final IpMuteData mute = itr.next();

        if (muteStorage.isMuted(mute.getIp()) && mute.getUpdated() < lastChecked) {
          continue;
        }

        muteStorage.addMute(mute);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  private void newUnmutes() {

    CloseableIterator<IpMuteRecord> itr = null;
    try {
      itr = plugin.getIpMuteRecordStorage().findUnmutes(lastChecked);

      while (itr.hasNext()) {
        final IpMuteRecord mute = itr.next();

        if (!muteStorage.isMuted(mute.getIp())) {
          continue;
        }

        muteStorage.removeMute(mute.getIp());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
