package me.confuser.banmanager.runnables;

import java.sql.SQLException;

import java.util.List;
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

            List<IpBanData> ip_ban_data = banStorage.findBans(lastChecked);

            for (IpBanData ban : ip_ban_data) {

                  if (banStorage.isBanned(ban.getIp()) && ban.getUpdated() < lastChecked) {
                        continue;
                  }

                  banStorage.addBan(ban);
            }

            ip_ban_data = null;
	}

	private void newUnbans() throws SQLException {

		List<IpBanRecord> ip_bans = plugin.getIpBanRecordStorage().findUnbans(lastChecked);

		for (IpBanRecord ban : ip_bans) {

			if (!banStorage.isBanned(ban.getIp()))
				continue;

			banStorage.removeBan(ban.getIp());

		}

		ip_bans = null;
	}
}
