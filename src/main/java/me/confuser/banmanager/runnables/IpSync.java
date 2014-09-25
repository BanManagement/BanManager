package me.confuser.banmanager.runnables;

import java.sql.SQLException;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.IpBanRecord;
import me.confuser.banmanager.storage.IpBanStorage;

public class IpSync implements Runnable {

      private BanManager plugin = BanManager.getPlugin();
      private IpBanStorage banStorage = plugin.getIpBanStorage();
      private long lastChecked = 0;

      public IpSync() {
            lastChecked = plugin.getSchedulesConfig().getLastChecked("ipBans");
      }

      @Override
      public void run() {
            // New/updated bans check
            try {
                  newBans();
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            // New unbans
            try {
                  newUnbans();
            } catch (SQLException e) {
                  e.printStackTrace();
            }

            lastChecked = System.currentTimeMillis() / 1000L;
            plugin.getSchedulesConfig().setLastChecked("ipBans", lastChecked);
      }

      private void newBans() throws SQLException {

            CloseableIterator<IpBanData> itr = banStorage.findBans(lastChecked);

            while (itr.hasNext()) {
                  final IpBanData ban = itr.next();

                  if (banStorage.isBanned(ban.getIp()) && ban.getUpdated() < lastChecked) {
                        continue;
                  }

                  banStorage.addBan(ban);
            }

            itr.close();
      }

      private void newUnbans() throws SQLException {

            CloseableIterator<IpBanRecord> itr = plugin.getIpBanRecordStorage().findUnbans(lastChecked);

            while (itr.hasNext()) {
                  final IpBanRecord ban = itr.next();

                  if (!banStorage.isBanned(ban.getIp())) {
                        continue;
                  }

                  banStorage.removeBan(ban.getIp());

            }

            itr.close();
      }
}
