package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.IpRangeBanRecord;
import me.confuser.banmanager.storage.IpRangeBanStorage;

import java.sql.SQLException;

public class IpRangeSync extends BmRunnable {

  private BanManager plugin = BanManager.getPlugin();
  private IpRangeBanStorage banStorage = plugin.getIpRangeBanStorage();

  public IpRangeSync() {
    super("ipRangeBans");
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
  }

  private void newBans() {

    CloseableIterator<IpRangeBanData> itr = null;
    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        final IpRangeBanData ban = itr.next();

        if (banStorage.isBanned(ban) && ban.getUpdated() < lastChecked) {
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

    CloseableIterator<IpRangeBanRecord> itr = null;
    try {
      itr = plugin.getIpRangeBanRecordStorage().findUnbans(lastChecked);

      while (itr.hasNext()) {
        final IpRangeBanRecord ban = itr.next();

        if (!banStorage.isBanned(ban.getRange())) {
          continue;
        }

        banStorage.removeBan(ban.getRange());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
