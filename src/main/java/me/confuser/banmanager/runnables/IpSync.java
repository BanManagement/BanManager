package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpBanRecord;
import me.confuser.banmanager.storage.IpBanStorage;

import java.sql.SQLException;

public class IpSync extends BmRunnable {

  private IpBanStorage banStorage = plugin.getIpBanStorage();

  public IpSync() {
    super("ipBans");
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
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
