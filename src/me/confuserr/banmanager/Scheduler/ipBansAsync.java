package me.confuserr.banmanager.Scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Database;
import me.confuserr.banmanager.data.IPBanData;

public class ipBansAsync implements Runnable {

	private Database localConn;
	private BanManager plugin;
	private long lastRun;

	public ipBansAsync(BanManager banManager) {
		plugin = banManager;
		localConn = plugin.localConn;
		lastRun = System.currentTimeMillis() / 1000;
	}

	@Override
	public void run() {
		// Check for new bans
		ResultSet result = localConn.query("SELECT * FROM " + localConn.ipBansTable + " WHERE ban_time > " + lastRun + "");

		try {
			while (result.next()) {
				// Add them to the banned list
				// First check to see if they aren't already in it, don't
				// want duplicates!
				if (!plugin.isIPBanned(result.getString("banned"))) {
					plugin.addIPBan(new IPBanData(result.getString("banned"), result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expires_on")));

					if (plugin.useBukkitBans())
						plugin.getServer().banIP(result.getString("banned"));
				}

			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check for old bans and remove them!
		ResultSet result1 = localConn.query("SELECT * FROM " + localConn.ipBansRecordTable + " WHERE ban_time > " + lastRun + "");

		try {
			while (result1.next()) {
				// Remove them from the list
				if (plugin.isIPBanned(result1.getString("banned"))) {
					plugin.getIPBans().remove(result1.getString("banned"));

					if (plugin.useBukkitBans())
						plugin.getServer().banIP(result1.getString("banned"));

				}

			}

			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		lastRun = System.currentTimeMillis() / 1000;

	}
}