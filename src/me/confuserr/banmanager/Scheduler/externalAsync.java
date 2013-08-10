package me.confuserr.banmanager.Scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.entity.Player;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Database;
import me.confuserr.banmanager.Util;

public class externalAsync implements Runnable {

	private Database extConn;
	private BanManager plugin;
	private long lastRun;

	public externalAsync(BanManager banManager, Database extConn, long lastRun) {
		plugin = banManager;
		this.extConn = extConn;
		this.lastRun = lastRun;
	}

	public void run() {
		// Player Bans
		// -----------------------------------------------------------------------------------------------------------------
		ResultSet result = extConn.query("SELECT * FROM " + extConn.getTable("bans") + " WHERE ban_time > " + lastRun + "");

		try {
			while (result.next()) {
				// Add them to the banned list
				// First check to see if they aren't already in it, don't
				// want duplicates!
				if (!plugin.isPlayerBanned(result.getString("banned"))) {
					plugin.addPlayerBan(result.getString("banned"), result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expires_on"));

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
				} else {
					// Perma bans override temp bans!
					if (plugin.getPlayerBan(result.getString("banned")).getExpires() != 0 && result.getLong("ban_expires_on") == 0) {
						plugin.removePlayerBan(result.getString("banned"), plugin.getMessage("consoleName"), true);
						plugin.addPlayerBan(result.getString("banned"), result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expires_on"));
					}
				}
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check for unbans and remove them!
		result = extConn.query("SELECT * FROM " + extConn.getTable("unbans") + " WHERE unban_time > " + lastRun + "");

		try {
			while (result.next()) {
				// Remove them from the list
				if (plugin.isPlayerBanned(result.getString("unbanned"))) {
					plugin.removePlayerBan(result.getString("unbanned"), result.getString("unbanned_by"), true);
				}

			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// IP Bans
		// -----------------------------------------------------------------------------------------------------------------
		result = extConn.query("SELECT * FROM " + extConn.getTable("ipBans") + " WHERE ban_time > " + lastRun + "");

		try {
			while (result.next()) {
				final String ip = result.getString("banned");
				// Add them to the banned list
				// First check to see if they aren't already in it, don't
				// want duplicates!
				if (!plugin.isIPBanned(ip)) {
					plugin.addIPBan(ip, result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expires_on"));
				} else {
					// Perma bans override temp bans!
					if (plugin.getIPBan(ip).getExpires() != 0 && result.getLong("ban_expires_on") == 0) {
						plugin.removeIPBan(ip, plugin.getMessage("consoleName"), true);
						plugin.addIPBan(ip, result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expires_on"));
					}
				}

				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
					public void run() {
						for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
							if (Util.getIP(onlinePlayer.getAddress().toString()).equals(ip)) {
								onlinePlayer.kickPlayer("Banned");
							}
						}
					}
				});
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check for unbans and remove them!
		result = extConn.query("SELECT * FROM " + extConn.getTable("ipUnbans") + " WHERE unban_time > " + lastRun + "");

		try {
			while (result.next()) {
				// Remove them from the list
				if (plugin.isIPBanned(result.getString("unbanned"))) {
					plugin.removeIPBan(result.getString("unbanned"), result.getString("unbanned_by"), true);
				}

			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Mutes
		// -----------------------------------------------------------------------------------------------------------------
		result = extConn.query("SELECT * FROM " + extConn.getTable("mutes") + " WHERE mute_time > " + lastRun + "");

		try {
			while (result.next()) {
				// Add them to the banned list
				// First check to see if they aren't already in it, don't
				// want duplicates!
				if (!plugin.isPlayerMuted(result.getString("muted"))) {
					plugin.addPlayerMute(result.getString("muted"), result.getString("muted_by"), result.getString("mute_reason"), result.getLong("mute_time"), result.getLong("mute_expires_on"));
				} else {
					// Perma bans override temp bans!
					if (plugin.getPlayerMute(result.getString("muted")).getExpires() != 0 && result.getLong("mute_expires_on") == 0) {
						plugin.removePlayerMute(result.getString("muted"), plugin.getMessage("consoleName"), true);
						plugin.addPlayerMute(result.getString("muted"), result.getString("muted_by"), result.getString("mute_reason"), result.getLong("mute_time"), result.getLong("mute_expires_on"));
					}
				}
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Check for unbans and remove them!
		result = extConn.query("SELECT * FROM " + extConn.getTable("unmutes") + " WHERE unmute_time > " + lastRun + "");

		try {
			while (result.next()) {
				// Remove them from the list
				if (plugin.isPlayerMuted(result.getString("unmuted"))) {
					plugin.removePlayerMute(result.getString("unmuted"), result.getString("unmuted_by"), true);
				}

			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		extConn.close();

		lastRun = System.currentTimeMillis() / 1000;
		save();
	}

	private synchronized void save() {
		plugin.schedulerFileConfig.set("lastChecked.external", lastRun);
		plugin.schedulerConfig.saveConfig();
	}
}
