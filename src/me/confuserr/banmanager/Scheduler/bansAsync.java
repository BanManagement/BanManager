package me.confuserr.banmanager.Scheduler;

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
					if (!plugin.bannedPlayers.contains(result.getString("banned").toLowerCase())) {
						plugin.bannedPlayers.add(result.getString("banned").toLowerCase());

						if (plugin.getServer().getPlayer(result.getString("banned")) != null) {
							// Oh, they're online, lets kick em!
							final String banned = result.getString("banned");

							plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

								@Override
								public void run() {
									plugin.getServer().getPlayer(banned).kickPlayer("Banned");
								}
							});
						}
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
					if (plugin.bannedPlayers.contains(result1.getString("banned").toLowerCase())) {
						plugin.bannedPlayers.remove(result1.getString("banned").toLowerCase());

						if (plugin.bukkitBan) {
							plugin.toUnbanPlayer.add(result1.getString("banned").toLowerCase());
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
