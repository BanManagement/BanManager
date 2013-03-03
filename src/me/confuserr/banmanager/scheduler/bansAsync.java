package me.confuserr.banmanager.scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Database;

public class bansAsync implements Runnable {

	private Database localConn;
	private BanManager plugin;
	private long lastRun;

	public bansAsync(BanManager banManager) {
		plugin = banManager;
		localConn = plugin.localConn;
		lastRun = System.currentTimeMillis() / 1000;
	}

	@Override
	public void run() {
		// Check for new bans
		ResultSet result = localConn.query("SELECT * FROM " + localConn.bansTable + " WHERE ban_time > " + lastRun + "");

		try {
			while (result.next()) {
				// Add them to the banned list
				synchronized (plugin.bannedPlayers) {
					// First check to see if they aren't already in it, don't
					// want duplicates!
					if (!plugin.bannedPlayers.contains(result.getString("banned"))) {
						plugin.bannedPlayers.add(result.getString("banned"));
					}
				}
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check for old bans and remove them!
		ResultSet result1 = localConn.query("SELECT * FROM " + localConn.bansRecordTable + " WHERE unbanned_time > " + lastRun + "");

		try {
			while (result1.next()) {
				// Remove them from the list

				synchronized (plugin.bannedPlayers) {
					if (plugin.bannedPlayers.contains(result1.getString("banned"))) {
						plugin.bannedPlayers.remove(result1.getString("banned"));
						
						if(plugin.bukkitBan) {
							plugin.toUnbanPlayer.add(result.getString("banned"));
						}
					}
				}
			}

			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		lastRun = System.currentTimeMillis() / 1000;

	}
}
