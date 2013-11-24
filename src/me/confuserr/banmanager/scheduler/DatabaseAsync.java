package me.confuserr.banmanager.scheduler;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.BanManager.CleanUp;
import me.confuserr.banmanager.Database;
import org.bukkit.Bukkit;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseAsync implements Runnable {

	private Database localConn;
	private ResultSet result;
	private BanManager plugin;

	public DatabaseAsync(BanManager banManager) {
		plugin = banManager;
		localConn = plugin.localConn;
	}

	public void run() {
		// Get current time as a unix timestamp
		long now = System.currentTimeMillis() / 1000;

		// First, the player bans
		localConn.query("INSERT INTO " + localConn.getTable("banRecords") + " (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, '" + plugin.getMessage("consoleName") + "', " + now + ", b.server FROM " + localConn.getTable("bans") + " b WHERE b.ban_expires_on != '0' AND b.ban_expires_on < '" + now + "'");

		// Now we need to unban them
		result = localConn.query("SELECT banned FROM " + localConn.getTable("bans") + " WHERE ban_expires_on != 0 AND ban_expires_on < '" + now + "'");

		try {
			while (result.next()) {

				final String playerName = result.getString("banned").toLowerCase();

				if (!plugin.isPlayerBanned(playerName))
					continue;

				plugin.getPlayerBans().remove(playerName);

				if (plugin.useBukkitBans()) {
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						public void run() {
							Bukkit.getOfflinePlayer(playerName).setBanned(false);
						}
					});
				}

			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Now remove it from the database
		localConn.query("DELETE FROM " + localConn.getTable("bans") + " WHERE ban_expires_on != '0' AND ban_expires_on < '" + now + "'");

		// Now, the IP bans
		localConn.query("INSERT INTO " + localConn.getTable("ipBanRecords") + " (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, '" + plugin.getMessage("consoleName") + "', " + now + ", b.server FROM " + localConn.getTable("ipBans") + " b WHERE b.ban_expires_on != '0' AND b.ban_expires_on < '" + now + "'");

		// Now we need to unban them
		result = localConn.query("SELECT banned FROM " + localConn.getTable("ipBans") + " WHERE ban_expires_on != '0' AND ban_expires_on < '" + now + "'");
		try {
			while (result.next()) {
				final String address = result.getString("banned");

				if (!plugin.isIPBanned(address))
					continue;

				plugin.getIPBans().remove(address);

				if (plugin.useBukkitBans()) {
					plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

						public void run() {
							Bukkit.unbanIP(address);
						}
					});
				}
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Now remove it from the database
		localConn.query("DELETE FROM " + localConn.getTable("ipBans") + " WHERE ban_expires_on != '0' AND ban_expires_on < '" + now + "'");

		// Now, the mutes
		localConn.query("INSERT INTO " + localConn.getTable("muteRecords") + " (muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time, server) SELECT b.muted, b.muted_by, b.mute_reason, b.mute_time, b.mute_expires_on, '" + plugin.getMessage("consoleName") + "', " + now + ", b.server FROM " + localConn.getTable("mutes") + " b WHERE b.mute_expires_on != '0' AND b.mute_expires_on < '" + now + "'");

		// Now we need to unmute them
		result = localConn.query("SELECT muted FROM " + localConn.getTable("mutes") + " WHERE mute_expires_on != '0' AND mute_expires_on < '" + now + "'");
		try {
			while (result.next()) {
				plugin.getPlayerMutes().remove(result.getString("muted").toLowerCase());
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Now remove it from the database
		localConn.query("DELETE FROM " + localConn.getTable("mutes") + " WHERE mute_expires_on != '0' AND mute_expires_on < '" + now + "'");

		// Now the kick logs if enabled
		if (CleanUp.Kicks.getDays() > 0) {
			localConn.query("DELETE FROM " + localConn.getTable("kicks") + " WHERE (kick_time + " + CleanUp.Kicks.getDaysInMilliseconds() + " ) < " + now + "");
		}

		if (CleanUp.PlayerIPs.getDays() > 0) {
			localConn.query("DELETE FROM " + localConn.getTable("playerIps") + " WHERE (last_seen + " + CleanUp.PlayerIPs.getDaysInMilliseconds() + " ) < " + now + "");
		}

		if (CleanUp.BanRecords.getDays() > 0) {
			localConn.query("DELETE FROM " + localConn.getTable("banRecords") + " WHERE (unbanned_time + " + CleanUp.BanRecords.getDaysInMilliseconds() + " ) < " + now + "");
		}

		if (CleanUp.IPBanRecords.getDays() > 0) {
			localConn.query("DELETE FROM " + localConn.getTable("ipBanRecords") + " WHERE (unbanned_time + " + CleanUp.IPBanRecords.getDaysInMilliseconds() + " ) < " + now + "");
		}

		if (CleanUp.MuteRecords.getDays() > 0) {
			localConn.query("DELETE FROM " + localConn.getTable("muteRecords") + " WHERE (unmuted_time + " + CleanUp.MuteRecords.getDaysInMilliseconds() + " ) < " + now + "");
		}

		if (CleanUp.Warnings.getDays() > 0) {
			localConn.query("DELETE FROM " + localConn.getTable("warnings") + " WHERE (warn_time + " + CleanUp.Warnings.getDaysInMilliseconds() + " ) < " + now + "");
		}

	}

}
