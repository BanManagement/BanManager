package me.confuserr.banmanager.scheduler;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Database;
import me.confuserr.banmanager.data.BanData;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BansAsync implements Runnable {

	private Database localConn;
	private BanManager plugin;
	private static long lastRun;

	public BansAsync(BanManager banManager, long lastChecked) {
		plugin = banManager;
		localConn = plugin.localConn;
		lastRun = lastChecked;
	}

	public void run() {
		// Check for new bans
        long thisRun = System.currentTimeMillis() / 1000; // Set the timestamp *before* we run, so we don't miss anything.

		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("bans") + " WHERE ban_time >= " + lastRun + "");

		try {
			while (result.next()) {
				// Add them to the banned list
				// First check to see if they aren't already in it, don't
				// want duplicates!
				if (!plugin.isPlayerBanned(result.getString("banned").toLowerCase())) {
					plugin.addPlayerBan(new BanData(result.getString("banned").toLowerCase(),
                            result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"),
                            result.getLong("ban_expires_on")), false);

					if (plugin.getServer().getPlayer(result.getString("banned")) != null) {
						// Oh, they're online, lets kick em!
						final String banned = result.getString("banned");

						plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

							public void run() {
								if (plugin.useBukkitBans())
									plugin.getServer().getPlayer(banned).setBanned(true);

								plugin.getServer().getPlayer(banned).kickPlayer("Banned");
							}
						});
					}
				}
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check for old bans and remove them!
		ResultSet result1 = localConn.query("SELECT * FROM " + localConn.getTable("banRecords") + " WHERE unbanned_time >= " + lastRun + "");

		try {
			while (result1.next()) {
				// Remove them from the list
				if (plugin.isPlayerBanned(result1.getString("banned").toLowerCase())) {
					plugin.getPlayerBans().remove(result1.getString("banned").toLowerCase());

					if (plugin.useBukkitBans())
						plugin.getServer().getOfflinePlayer(result1.getString("banned").toLowerCase()).setBanned(false);
				}

			}

			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

        lastRun = thisRun;
	}
	
	public static long getLastRun() {
		return lastRun;
	}
}
