package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpBanRecord;
import me.confuser.banmanager.storage.IpBanStorage;

import java.sql.SQLException;

public class IpSync implements Runnable {

  private BanManager plugin = BanManager.getPlugin();
  private IpBanStorage banStorage = plugin.getIpBanStorage();
  private long lastChecked = 0;
  @Getter
  private boolean isRunning = false;

  public IpSync() {
    lastChecked = plugin.getSchedulesConfig().getLastChecked("ipBans");
  }

  @Override
  public void run() {
    if (isRunning) return;

    isRunning = true;
    // New/updated bans check
    newBans();

    // New unbans
    newUnbans();

    lastChecked = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastChecked("ipBans", lastChecked);
    isRunning = false;
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
}
