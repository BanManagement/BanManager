package me.confuserr.banmanager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import me.confuserr.banmanager.data.*;

public class DbLogger {
	private Database localConn;
	private BanManager plugin;

	DbLogger(Database conn, BanManager instance) {
		localConn = conn;
		plugin = instance;
	}

	public void logBan(String name, String bannedBy, String reason, long time, long expires) {
		Util.asyncQuery("INSERT INTO " + localConn.getTable("bans") + " (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('" + name + "', '" + bannedBy + "', '" + reason + "', '" + time + "', '" + expires + "', '" + plugin.serverName + "')");
	}

	public void logIpBan(String name, String bannedBy, String reason, long time, long expires) {
		Util.asyncQuery("INSERT INTO " + localConn.getTable("ipBans") + " (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('" + name + "', '" + bannedBy + "', '" + reason + "', '" + time + "', '" + expires + "', '" + plugin.serverName + "')");
	}

	public void logKick(String name, String bannedBy, String reason) {
		Util.asyncQuery("INSERT INTO " + localConn.getTable("kicks") + " (kicked, kicked_by, kick_reason, kick_time, server) VALUES ('" + name + "', '" + bannedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '" + plugin.serverName + "')");
	}

	public void logMute(String name, String mutedBy, String reason, long time, long expires) {
		Util.asyncQuery("INSERT INTO " + localConn.getTable("mutes") + " (muted, muted_by, mute_reason, mute_time, mute_expires_on, server) VALUES ('" + name + "', '" + mutedBy + "', '" + reason + "', '" + time + "', '" + expires + "', '" + plugin.serverName + "')");
	}

	public void logWarning(String name, String warnedBy, String reason) {
		Util.asyncQuery("INSERT INTO " + localConn.getTable("warnings") + " (warned, warned_by, warn_reason, warn_time, server) VALUES ('" + name + "', '" + warnedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '" + plugin.serverName + "')");
	}

	// External Logs
	public void logBanAll(Database extConn, String name, String bannedBy, String reason) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("bans") + " (banned, banned_by, ban_reason, ban_time, ban_expires_on) VALUES ('" + name + "', '" + bannedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '0')", extConn);
	}

	public void logTempBanAll(Database extConn, String name, String bannedBy, String reason, long expires) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("bans") + " (banned, banned_by, ban_reason, ban_time, ban_expires_on) VALUES ('" + name + "', '" + bannedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '" + expires + "')", extConn);
	}

	public void logIPBanAll(Database extConn, String name, String bannedBy, String reason) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("ipBans") + " (banned, banned_by, ban_reason, ban_time, ban_expires_on) VALUES ('" + name + "', '" + bannedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '0')", extConn);
	}

	public void logTempIPBanAll(Database extConn, String name, String bannedBy, String reason, long expires) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("ipBans") + " (banned, banned_by, ban_reason, ban_time, ban_expires_on) VALUES ('" + name + "', '" + bannedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '" + expires + "')", extConn);
	}

	public void logMuteAll(Database extConn, String name, String mutedBy, String reason) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("mutes") + " (muted, muted_by, mute_reason, mute_time, mute_expires_on) VALUES ('" + name + "', '" + mutedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '0')", extConn);
	}

	public void logTempMuteAll(Database extConn, String name, String mutedBy, String reason, long expires) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("mutes") + " (muted, muted_by, mute_reason, mute_time, mute_expires_on) VALUES ('" + name + "', '" + mutedBy + "', '" + reason + "', UNIX_TIMESTAMP(now()), '" + expires + "')", extConn);
	}

	public boolean handleBukkitBan(String name) {
		ResultSet result2 = localConn.query("SELECT banned FROM " + localConn.getTable("banRecords") + " WHERE banned = '" + name + "'");

		try {
			if (result2.next()) {
				plugin.getServer().getOfflinePlayer(name).setBanned(false);
			}
			result2.close();
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	public int getPastBanCount(String name) {
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM " + localConn.getTable("banRecords") + " WHERE banned = '" + name + "'");
		int count = 0;
		try {
			if (result.next())
				count = result.getInt("numb");
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return count;
	}

	public int getPastMuteCount(String user) {
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM " + localConn.getTable("muteRecords") + " WHERE muted = '" + user + "'");
		int count = 0;
		try {
			if (result.next())
				count = result.getInt("numb");
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return count;
	}

	public int getWarningCount(String user) {
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM " + localConn.getTable("warnings") + " WHERE warned = '" + user + "'");
		int count = 0;
		try {
			if (result.next())
				count = result.getInt("numb");
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return count;
	}

	public boolean isMuted(String name) {
		boolean muted = false;

		ResultSet result = localConn.query("SELECT mute_id FROM " + localConn.getTable("mutes") + " WHERE muted = '" + name + "'");
		try {
			if (result.next())
				muted = true;
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return muted;
	}

	public MuteData getMute(String name) {
		MuteData data = null;

		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("mutes") + " WHERE muted = '" + name + "'");
		try {
			if (result.next())
				data = new MuteData(name, result.getString("muted_by"), result.getString("mute_reason"), result.getLong("mute_time"), result.getLong("mute_expires_on"));
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return data;
	}

	public ArrayList<MuteData> getPastMutes(String name) {
		ArrayList<MuteData> data = new ArrayList<MuteData>();

		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("muteRecords") + " WHERE muted = '" + name + "'");
		try {
			while (result.next()) {
				data.add(new MuteData(name, result.getString("muted_by"), result.getString("mute_reason"), result.getLong("mute_time"), result.getLong("mute_expired_on")));
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return data;
	}

	public ArrayList<BanData> getPastBans(String name) {
		ArrayList<BanData> data = new ArrayList<BanData>();

		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("banRecords") + " WHERE banned = '" + name + "'");
		try {
			while (result.next()) {
				data.add(new BanData(name, result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expired_on")));
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return data;
	}

	public ArrayList<IPBanData> getPastIPBans(String ip) {
		ArrayList<IPBanData> data = new ArrayList<IPBanData>();

		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("ipBanRecords") + " WHERE banned = '" + ip + "'");
		try {
			while (result.next()) {
				data.add(new IPBanData(ip, result.getString("banned_by"), result.getString("ban_reason"), result.getLong("ban_time"), result.getLong("ban_expired_on")));
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return data;
	}

	public ArrayList<WarnData> getWarnings(String name) {
		ArrayList<WarnData> data = new ArrayList<WarnData>();

		ResultSet result = localConn.query("SELECT * FROM " + localConn.getTable("warnings") + " WHERE warned = '" + name + "'");
		try {
			while (result.next()) {
				data.add(new WarnData(name, result.getString("warned_by"), result.getString("warn_reason"), result.getLong("warn_time")));
			}

			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return data;
	}

	public void removeWarnings(String name) {
		Util.asyncQuery("DELETE FROM " + localConn.getTable("warnings") + " WHERE warned = '" + name + "'");
	}

	public void banRemove(String name, String by, boolean keepLog) {
		if (keepLog)
			Util.asyncQuery("INSERT INTO " + localConn.getTable("banRecords") + " (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, \"" + by + "\", UNIX_TIMESTAMP(now()), b.server FROM " + localConn.getTable("bans") + " b WHERE b.banned = '" + name + "'");

		// Now delete it
		Util.asyncQuery("DELETE FROM " + localConn.getTable("bans") + " WHERE banned = '" + name + "'");
	}

	public void banExternalRemove(Database extConn, String name, String by) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("unbans") + " (unbanned, unbanned_by, unban_time) VALUES ('" + name + "', '" + by + "', UNIX_TIMESTAMP(now()))", extConn);
	}

	public void banRemoveRecords(String name) {
		Util.asyncQuery("DELETE FROM " + localConn.getTable("banRecords") + " WHERE banned = '" + name + "'");
	}

	public void ipRemove(String ip, String by, boolean keepLog) {
		if (keepLog)
			Util.asyncQuery("INSERT INTO " + localConn.getTable("ipBanRecords") + " (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, \"" + by + "\", UNIX_TIMESTAMP(now()), b.server  FROM " + localConn.getTable("ipBans") + " b WHERE b.banned = '" + ip + "'");

		// Now delete it
		Util.asyncQuery("DELETE FROM " + localConn.getTable("ipBans") + " WHERE banned = '" + ip + "'");
	}

	public void ipExternalRemove(Database extConn, String ip, String by) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("ipUnbans") + " (unbanned, unbanned_by, unban_time) VALUES ('" + ip + "', '" + by + "', UNIX_TIMESTAMP(now()))", extConn);
	}

	public void muteRemove(String name, String by, boolean keepLog) {
		if (keepLog)
			Util.asyncQuery("INSERT INTO " + localConn.getTable("muteRecords") + " (muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time, server) SELECT b.muted, b.muted_by, b.mute_reason, b.mute_time, b.mute_expires_on, \"" + by + "\", UNIX_TIMESTAMP(now()), b.server FROM " + localConn.getTable("mutes") + " b WHERE b.muted = '" + name + "'");

		// Now delete it
		Util.asyncQuery("DELETE FROM " + localConn.getTable("mutes") + " WHERE muted = '" + name + "'");
	}

	public void muteRemoveRecords(String name) {
		Util.asyncQuery("DELETE FROM " + localConn.getTable("muteRecords") + " WHERE muted = '" + name + "'");
	}

	public void muteExternalRemove(Database extConn, String name, String by) {
		Util.asyncQuery("INSERT INTO " + extConn.getTable("unmutes") + " (unmuted, unmuted_by, unmute_time) VALUES ('" + name + "', '" + by + "', UNIX_TIMESTAMP(now()))", extConn);
	}

	public void kickRemoveRecords(String name) {
		Util.asyncQuery("DELETE FROM " + localConn.getTable("kicks") + " WHERE kicked = '" + name + "'");
	}

	public void create_tables() throws SQLException {
		boolean Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("bans") + " (" + "ban_id int(255) NOT NULL AUTO_INCREMENT," + "banned varchar(32) NOT NULL," + "banned_by varchar(32) NOT NULL," + "ban_reason text NOT NULL," + "ban_time int(10) NOT NULL," + "ban_expires_on int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (ban_id)," + "KEY `banned` (`banned`), INDEX `ban_time` (`ban_time`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

		if (!Table)
			plugin.getLogger().severe("Unable to create local BanManagement table");
		else {
			Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("banRecords") + " (" + "ban_record_id int(255) NOT NULL AUTO_INCREMENT," + "banned varchar(32) NOT NULL," + "banned_by varchar(32) NOT NULL," + "ban_reason text NOT NULL," + "ban_time int(10) NOT NULL," + "ban_expired_on int(10) NOT NULL," + "unbanned_by varchar(32) NOT NULL," + "unbanned_time int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (ban_record_id)," + "KEY `banned` (`banned`), INDEX `ban_time` (`ban_time`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

			if (!Table)
				plugin.getLogger().severe("Unable to create local BanManagement table");
			else {
				Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("ipBans") + " (" + "ban_id int(255) NOT NULL AUTO_INCREMENT," + "banned varchar(32) NOT NULL," + "banned_by varchar(32) NOT NULL," + "ban_reason text NOT NULL," + "ban_time int(10) NOT NULL," + "ban_expires_on int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (ban_id)," + "KEY `banned` (`banned`), INDEX `ban_time` (`ban_time`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

				if (!Table)
					plugin.getLogger().severe("Unable to create local BanManagement table");
				else {
					Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("ipBanRecords") + " (" + "ban_record_id int(255) NOT NULL AUTO_INCREMENT," + "banned varchar(32) NOT NULL," + "banned_by varchar(32) NOT NULL," + "ban_reason text NOT NULL," + "ban_time int(10) NOT NULL," + "ban_expired_on int(10) NOT NULL," + "unbanned_by varchar(32) NOT NULL," + "unbanned_time int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (ban_record_id)," + "KEY `banned` (`banned`), INDEX `ban_time` (`ban_time`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

					if (!Table)
						plugin.getLogger().severe("Unable to create local BanManagement table");
					else {
						Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("kicks") + " (" + "kick_id int(255) NOT NULL AUTO_INCREMENT," + "kicked varchar(32) NOT NULL," + "kicked_by varchar(32) NOT NULL," + "kick_reason text NOT NULL," + "kick_time int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (kick_id)," + "KEY `kicked` (`kicked`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
						if (!Table)
							plugin.getLogger().severe("Unable to create local BanManagement table");
						else {
							Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("mutes") + " (" + "mute_id int(255) NOT NULL AUTO_INCREMENT," + "muted varchar(32) NOT NULL," + "muted_by varchar(32) NOT NULL," + "mute_reason text NOT NULL," + "mute_time int(10) NOT NULL," + "mute_expires_on int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (mute_id)," + "KEY `muted` (`muted`), INDEX `mute_time` (`mute_time`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

							if (!Table)
								plugin.getLogger().severe("Unable to create local BanManagement table");
							else {
								Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("muteRecords") + " (" + "mute_record_id int(255) NOT NULL AUTO_INCREMENT," + "muted varchar(32) NOT NULL," + "muted_by varchar(32) NOT NULL," + "mute_reason text NOT NULL," + "mute_time int(10) NOT NULL," + "mute_expired_on int(10) NOT NULL," + "unmuted_by varchar(32) NOT NULL," + "unmuted_time int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (mute_record_id)," + "KEY `muted` (`muted`), INDEX `ban_time` (`mute_time`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

								if (!Table)
									plugin.getLogger().severe("Unable to create local BanManagement table");
								else {
									Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("playerIps") + " (" + "`player` varchar(25) NOT NULL," + "`ip` int UNSIGNED NOT NULL," + "`last_seen` int(10) NOT NULL," + "PRIMARY KEY `player` (`player`)," + "KEY `ip` (`ip`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

									if (!Table)
										plugin.getLogger().severe("Unable to create local BanManagement table");
									else {
										Table = localConn.createTable("CREATE TABLE IF NOT EXISTS " + localConn.getTable("warnings") + " (" + "warn_id int(255) NOT NULL AUTO_INCREMENT," + "warned varchar(32) NOT NULL," + "warned_by varchar(32) NOT NULL," + "warn_reason text NOT NULL," + "warn_time int(10) NOT NULL," + "server varchar(30) NOT NULL," + "PRIMARY KEY (warn_id)," + "KEY `kicked` (`warned`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

										if (!Table)
											plugin.getLogger().severe("Unable to create local BanManagement table");
									}

									/*
									 * if(!Table) plugin.getLogger().severe(
									 * "Unable to create local BanManagement table"
									 * ); else { Table = localConn.createTable(
									 * "CREATE TABLE IF NOT EXISTS "
									 * +localConn.banAppealsTable+" ("+
									 * "`appeal_id` int(255) NOT NULL AUTO_INCREMENT,"
									 * + "`ban_id` int(255) NOT NULL," +
									 * "`ban_type` int(1) NOT NULL,"+
									 * "`appeal_time` int(10) NOT NULL,"+
									 * "PRIMARY KEY `player` (`player`),"+
									 * "KEY `ip` (`ip`)"+
									 * ") ENGINE=MyISAM  DEFAULT CHARSET=latin1"
									 * );
									 * 
									 * if(!Table) plugin.getLogger().severe(
									 * "Unable to create local BanManagement table"
									 * ); else { Table = localConn.createTable(
									 * "CREATE TABLE IF NOT EXISTS "
									 * +localConn.pinsTable+" ("+
									 * "`pin_id` int(255) UNSIGNED NOT NULL AUTO_INCREMENT,"
									 * + "`player` varchar(25) NOT NULL," +
									 * "`ban_type` int(1) NOT NULL,"+
									 * "`appeal_time` int(10) NOT NULL,"+
									 * "PRIMARY KEY `player` (`player`),"+
									 * "KEY `ip` (`ip`)"+
									 * ") ENGINE=MyISAM  DEFAULT CHARSET=latin1"
									 * );
									 * 
									 * if(!Table) plugin.getLogger().severe(
									 * "Unable to create local BanManagement table"
									 * ); else { Table = localConn.createTable(
									 * "CREATE TABLE IF NOT EXISTS "
									 * +localConn.staffTable+" ("+
									 * "`staff_id` int(255) UNSIGNED NOT NULL AUTO_INCREMENT,"
									 * + "`ssid` varchar(32) NOT NULL,"+
									 * "`player` varchar(25) NOT NULL," +
									 * "`permissions` int(255) UNSIGNED NOT NULL,"
									 * +
									 * "`password_hash` varchar(40) NOT NULL,"+
									 * "`password_salt` varchar(10),"+
									 * "PRIMARY KEY `staff_id` (`staff_id`),"+
									 * "KEY `ssid` (`ssid`)"+
									 * ") ENGINE=MyISAM  DEFAULT CHARSET=latin1"
									 * );
									 * 
									 * if(!Table) plugin.getLogger().severe(
									 * "Unable to create local BanManagement table"
									 * ); } } }
									 */
								}
							}
						}
					}
				}
			}
		}
	}

	public void createExternalTables(Database extConn) throws SQLException {
		boolean Table = extConn.createTable("CREATE TABLE IF NOT EXISTS " + extConn.getTable("bans") + " (" + "ban_id int(255) NOT NULL AUTO_INCREMENT," + "banned varchar(32) NOT NULL," + "banned_by varchar(32) NOT NULL," + "ban_reason text NOT NULL," + "ban_time int(10) NOT NULL," + "ban_expires_on int(10) NOT NULL," + "PRIMARY KEY (ban_id)," + "KEY `banned` (`banned`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

		if (!Table)
			plugin.getLogger().severe("Unable to create external BanManagement table");
		else {
			Table = extConn.createTable("CREATE TABLE IF NOT EXISTS " + extConn.getTable("unbans") + " (" + "unban_id int(255) NOT NULL AUTO_INCREMENT," + "unbanned varchar(32) NOT NULL," + "unbanned_by varchar(32) NOT NULL," + "unban_time int(10) NOT NULL," + "PRIMARY KEY (unban_id)," + "KEY `unbanned` (`unbanned`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

			if (!Table)
				plugin.getLogger().severe("Unable to create external BanManagement table");
			else {
				Table = extConn.createTable("CREATE TABLE IF NOT EXISTS " + extConn.getTable("ipBans") + " (" + "ban_id int(255) NOT NULL AUTO_INCREMENT," + "banned varchar(32) NOT NULL," + "banned_by varchar(32) NOT NULL," + "ban_reason text NOT NULL," + "ban_time int(10) NOT NULL," + "ban_expires_on int(10) NOT NULL," + "PRIMARY KEY (ban_id)," + "KEY `banned` (`banned`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

				if (!Table)
					plugin.getLogger().severe("Unable to create external BanManagement table");
				else {
					Table = extConn.createTable("CREATE TABLE IF NOT EXISTS " + extConn.getTable("ipUnbans") + " (" + "unban_id int(255) NOT NULL AUTO_INCREMENT," + "unbanned varchar(32) NOT NULL," + "unbanned_by varchar(32) NOT NULL," + "unban_time int(10) NOT NULL," + "PRIMARY KEY (unban_id)," + "KEY `unbanned` (`unbanned`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

					if (!Table)
						plugin.getLogger().severe("Unable to create external BanManagement table");
					else {
						Table = extConn.createTable("CREATE TABLE IF NOT EXISTS " + extConn.getTable("mutes") + " (" + "mute_id int(255) NOT NULL AUTO_INCREMENT," + "muted varchar(32) NOT NULL," + "muted_by varchar(32) NOT NULL," + "mute_reason text NOT NULL," + "mute_time int(10) NOT NULL," + "mute_expires_on int(10) NOT NULL," + "PRIMARY KEY (mute_id)," + "KEY `muted` (`muted`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");

						if (!Table)
							plugin.getLogger().severe("Unable to create external BanManagement table");
						else {
							Table = extConn.createTable("CREATE TABLE IF NOT EXISTS " + extConn.getTable("unmutes") + " (" + "unmute_id int(255) NOT NULL AUTO_INCREMENT," + "unmuted varchar(32) NOT NULL," + "unmuted_by varchar(32) NOT NULL," + "unmute_time int(10) NOT NULL," + "PRIMARY KEY (unmute_id)," + "KEY `unmuted` (`unmuted`)" + ") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
						}
					}
				}
			}
		}
	}

	public void setIP(String name, String ip) {
		Util.asyncQuery("INSERT INTO " + localConn.getTable("playerIps") + " (`player`, `ip`, `last_seen`) VALUES ('" + name + "', INET_ATON('" + ip + "'), '" + System.currentTimeMillis() / 1000 + "') ON DUPLICATE KEY UPDATE ip = INET_ATON('" + Util.getIP(ip) + "'), last_seen = '" + System.currentTimeMillis() / 1000 + "'");
	}

	public String getIP(String name) {
		String ip = "";

		ResultSet result = localConn.query("SELECT INET_NTOA(ip) AS ipAddress FROM " + localConn.getTable("playerIps") + " WHERE player = '" + name + "'");

		try {
			if (result.next())
				ip = result.getString("ipAddress");
			result.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return ip;
	}

	public String findPlayerIpDuplicates(String ip, String player) {
		ResultSet result = localConn.query("SELECT ip.player FROM " + localConn.getTable("bans") + " b LEFT JOIN " + localConn.getTable("playerIps") + " ip ON ip.player = b.banned WHERE ip.ip = INET_ATON('" + ip + "')");

		try {
			if (!result.isBeforeFirst()) {
				result.close();
				return "";
			} else {

				ArrayList<String> playerList = new ArrayList<String>();

				while (result.next()) {
					if (!playerList.contains(result.getString("player")) && !result.getString("player").equals(player)) {
						playerList.add(result.getString("player"));
					}
				}

				result.close();

				if (playerList.size() == 0)
					return "";

				String players = "";

				for (String p : playerList) {
					players += p + ", ";
				}

				return players.substring(0, players.length() - 2);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return "";
	}

	public void closeConnection() {
		localConn.close();
	}
}