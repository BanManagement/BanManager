package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.IpRangeBanRecord;
import me.confuser.banmanager.common.storage.IpRangeBanStorage;

import java.sql.SQLException;

public class IpRangeSync extends BmRunnable {

  private IpRangeBanStorage banStorage;

  public IpRangeSync(BanManagerPlugin plugin) {
    super(plugin, "ipRangeBans");

    banStorage = plugin.getIpRangeBanStorage();
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

        if (banStorage.isBanned(ban)) {
          if (ban.getUpdated() < lastChecked) continue;

          if (ban.equalsBan(banStorage.getBan(ban.getRange()))) {
            continue;
          }
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
