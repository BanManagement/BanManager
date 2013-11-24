package me.confuserr.banmanager.Scheduler;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Database;
import me.confuserr.banmanager.data.IPBanData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ipBansAsync implements Runnable {

	private Database localConn;
	private BanManager plugin;
	private long lastRun;

	public ipBansAsync(BanManager banManager, long lastChecked) {
		plugin = banManager;
		localConn = plugin.localConn;
		lastRun = lastChecked;
	}

	public void run() {
		// Check for new bans
        long thisRun = System.currentTimeMillis() / 1000;
		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("ipBans") + " WHERE ban_time >= " + lastRun + "");

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
		ResultSet result1 = localConn.query("SELECT * FROM " + localConn.getTable("ipBanRecords") + " WHERE ban_time >= " + lastRun + "");

		try {
			while (result1.next()) {
				// Remove them from the list
				if (plugin.isIPBanned(result1.getString("banned"))) {
					plugin.getIPBans().remove(result1.getString("banned"));

					if (plugin.useBukkitBans())
						plugin.getServer().unbanIP(result1.getString("banned"));

				}

			}

			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

        lastRun = thisRun;
		save();

	}
	
	private synchronized void save() {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

			@Override
			public void run() {
				plugin.schedulerFileConfig.set("lastChecked.ipbans", lastRun);
				plugin.schedulerConfig.saveConfig();
			}

		});
	}
}