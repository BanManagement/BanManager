package me.confuserr.banmanager.scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Database;

public class databaseAsync implements Runnable {

	private Database localConn;
	private ResultSet result;
	private BanManager plugin;
	int kickDays, banDays, ipDays, muteDays, playerIPDays;

	public databaseAsync(BanManager banManager) {
		plugin = banManager;
		localConn = plugin.localConn;

		kickDays = 86400 * plugin.keepKicks;
		banDays = 86400 * plugin.keepBanRecords;
		ipDays = 86400 * plugin.keepIPBanRecords;
		muteDays = 86400 * plugin.keepMuteRecords;
		playerIPDays = 86400 * plugin.keepIPs;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		// Get current time as a unix timestamp
		long now = System.currentTimeMillis() / 1000;

		// First, the player bans
		localConn.query("INSERT INTO " + localConn.bansRecordTable + " (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, 'Console automated', " + now + ", b.server FROM " + localConn.bansTable + " b WHERE b.ban_expires_on != '0' AND b.ban_expires_on < '" + now + "'");

		if (plugin.bukkitBan) {
			// Now we need to bukkit unban them
			result = localConn.query("SELECT banned FROM " + localConn.bansTable + " WHERE ban_expires_on != 0 AND ban_expires_on < '" + now + "'");
			try {
				while (result.next()) {
					plugin.toUnbanPlayer.add(result.getString("banned").toLowerCase());
				}
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// Now remove it from the database
		localConn.query("DELETE FROM " + localConn.bansTable + " WHERE ban_expires_on != '0' AND ban_expires_on < '" + now + "'");

		// Now, the IP bans
		localConn.query("INSERT INTO " + localConn.ipBansRecordTable + " (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, 'Console automated', " + now + ", b.server FROM " + localConn.ipBansTable + " b WHERE b.ban_expires_on != '0' AND b.ban_expires_on < '" + now + "'");

		if (plugin.bukkitBan) {
			// Now we need to bukkit unban them
			result = localConn.query("SELECT banned FROM " + localConn.ipBansTable + " WHERE ban_expires_on != '0' AND ban_expires_on < '" + now + "'");
			try {
				while (result.next()) {
					plugin.toUnbanIp.add(result.getString("banned"));
				}
				result.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		// Now remove it from the database
		localConn.query("DELETE FROM " + localConn.ipBansTable + " WHERE ban_expires_on != '0' AND ban_expires_on < '" + now + "'");

		// Now, the mutes
		localConn.query("INSERT INTO " + localConn.mutesRecordTable + " (muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time, server) SELECT b.muted, b.muted_by, b.mute_reason, b.mute_time, b.mute_expires_on, 'Console automated', " + now + ", b.server FROM " + localConn.mutesTable + " b WHERE b.mute_expires_on != '0' AND b.mute_expires_on < '" + now + "'");

		// Now we need to unmute them
		result = localConn.query("SELECT muted FROM " + localConn.mutesTable + " WHERE mute_expires_on != '0' AND mute_expires_on < '" + now + "'");
		try {
			while (result.next()) {
				plugin.removeHashMute(result.getString("muted"));
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		// Now remove it from the database
		localConn.query("DELETE FROM " + localConn.mutesTable + " WHERE mute_expires_on != '0' AND mute_expires_on < '" + now + "'");

		// Now the kick logs if enabled
		if (plugin.keepKicks > 0) {
			localConn.query("DELETE FROM " + localConn.kicksTable + " WHERE (kick_time + " + kickDays + " ) < " + now + "");
		}

		if (plugin.keepIPs > 0) {
			localConn.query("DELETE FROM " + localConn.playerIpsTable + " WHERE (last_seen + " + playerIPDays + " ) < " + now + "");
		}

		if (plugin.keepBanRecords > 0) {
			localConn.query("DELETE FROM " + localConn.bansRecordTable + " WHERE (unbanned_time + " + banDays + " ) < " + now + "");
		}
		
		if (plugin.keepIPBanRecords > 0) {
			localConn.query("DELETE FROM " + localConn.ipBansRecordTable + " WHERE (unbanned_time + " + ipDays + " ) < " + now + "");
		}
		
		if (plugin.keepMuteRecords > 0) {
			localConn.query("DELETE FROM " + localConn.mutesRecordTable + " WHERE (unmute_time + " + muteDays + " ) < " + now + "");
		}

	}

}
