package me.confuserr.banmanager;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.MuteData;

import org.bukkit.ChatColor;


public class DbLogger {
	private Database localConn;
	private String bansTable;
	private String recordsTable;
	private String ipBansTable;
	private String ipRecordsTable;
	private BanManager plugin;
	
	DbLogger(Database conn, String localBansTable, String localBanRecordTable, String localIpBansTable, String localIpBanRecordTable, BanManager instance) {
		localConn = conn;
		bansTable = localBansTable;
		recordsTable = localBanRecordTable;
		ipBansTable = localIpBansTable;
		ipRecordsTable = localIpBanRecordTable;
		plugin = instance;
	}
	
	public void logBan(String banned, String banned_by, String reason) {
		plugin.asyncQuery("INSERT INTO "+bansTable+" (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '0', '"+plugin.serverName+"')");
		plugin.bannedPlayers.add(banned);
	}
	
	public void logTempBan(String banned, String banned_by, String reason, long expires) {
		plugin.asyncQuery("INSERT INTO "+bansTable+" (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+expires+"', '"+plugin.serverName+"')");
		plugin.bannedPlayers.add(banned);
	}
	
	public void logIpBan(String banned, String banned_by, String reason) {
		plugin.asyncQuery("INSERT INTO "+ipBansTable+" (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '0', '"+plugin.serverName+"')");
		plugin.bannedIps.add(banned);
	}
	
	public void logKick(String banned, String banned_by, String reason) {
		plugin.asyncQuery("INSERT INTO "+plugin.localKicksTable+" (kicked, kicked_by, kick_reason, kick_time, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+plugin.serverName+"')");
	}
	
	public void logMute(String muted, String muted_by, String reason) {
		plugin.asyncQuery("INSERT INTO "+plugin.localMutesTable+" (muted, muted_by, mute_reason, mute_time, mute_expires_on, server) VALUES ('"+muted+"', '"+muted_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '0', '"+plugin.serverName+"')");
	}
	
	public void logTempMute(String muted, String muted_by, String reason, long expires) {
		plugin.asyncQuery("INSERT INTO "+plugin.localMutesTable+" (muted, muted_by, mute_reason, mute_time, mute_expires_on, server) VALUES ('"+muted+"', '"+muted_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+expires+"', '"+plugin.serverName+"')");
	}
	
	public BanData getCurrentBan(String username) {
		
		ResultSet result = localConn.query("SELECT ban_id, ban_reason, banned_by, ban_time, ban_expires_on FROM "+bansTable+" WHERE banned = '"+username+"'");
		try {
			if(result.next()) {
				
				BanData data = new BanData(username, result.getLong("ban_expires_on"), result.getString("ban_reason"), result.getLong("ban_time"), result.getString("banned_by"));
				
				result.close();
				
				return data;
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String isBanned(String username) {
		String message = "";
		
		ResultSet result = localConn.query("SELECT ban_id, ban_reason, banned_by, ban_time, ban_expires_on FROM "+bansTable+" WHERE banned = '"+username+"'");
		try {
			if(result.next()) {
				// Found, check to see if perma banned
				// But first, we see if they are bukkit banned, if not we make it so
				if(!plugin.getServer().getOfflinePlayer(username).isBanned()) {
					plugin.getServer().getOfflinePlayer(username).setBanned(true);
				}
				long expires = result.getInt("ban_expires_on");
				String reason = plugin.viewReason(result.getString("ban_reason"));
				String by = result.getString("banned_by");
				
				if(expires == 0) {
					// Perma banned
					message = plugin.banMessages.get("disconnectBan").replace("[reason]", reason).replace("[name]", username).replace("[by]", by);
				} else {
					// Temp ban, check to see if expired
					long timestampNow = System.currentTimeMillis()/1000;
					if(timestampNow < expires) {
						// Still banned
						expires = (long) expires * 1000;
						String formatExpires = plugin.formatDateDiff(expires);
						message = plugin.banMessages.get("disconnectTempBan").replace("[name]", username).replace("[expires]", formatExpires).replace("[reason]", reason).replace("[by]", by);
					} else {
						// No longer banned, remove the ban!
						if(plugin.bukkitBan)
							plugin.getServer().getOfflinePlayer(username).setBanned(false);

						banRemove(result.getInt("ban_id"), "Console automated");
					}
				}
			} else if(plugin.getServer().getOfflinePlayer(username).isBanned() && plugin.bukkitBan) {
				// Not in the current bans, but they are banned by bukkit
				// Check if they've been previously banned, if they have, unban them
				// Not unbanning without this check in case they were banned before the plugin was installed
				ResultSet result2 = localConn.query("SELECT banned FROM "+recordsTable+" WHERE banned = '"+username+"'");
				if(result2.next())
					plugin.getServer().getOfflinePlayer(username).setBanned(false);
				
				result2.close();
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	// IP ban check
	public String isBanned(InetAddress address) {
		String message = "";
		String ip = plugin.getIp(address.toString());
		
		ResultSet result = localConn.query("SELECT ban_id, ban_reason, banned_by, ban_time, ban_expires_on FROM "+ipBansTable+" WHERE banned = '"+ip+"'");
		try {
			if(result.next()) {
				// Found, check to see if perma banned
				// But first, we see if they are bukkit banned, if not we make it so
				if(!ipBanned(ip))
					plugin.getServer().banIP(ip);
				
				long expires = result.getInt("ban_expires_on");
				String reason = plugin.viewReason(result.getString("ban_reason"));
				String by = result.getString("banned_by");
				
				if(expires == 0) {
					// Perma banned
					message = plugin.banMessages.get("disconnectIpBan").replace("[ip]", ip).replace("[reason]", reason).replace("[by]", by);
				}
			} else if(ipBanned(ip) && plugin.bukkitBan) {
				// Not in the current bans, but they are banned by bukkit
				// Check if they've been previously banned, if they have, unban them
				// Not unbanning without this check in case they were banned before the plugin was installed
				ResultSet result2 = localConn.query("SELECT banned FROM "+ipRecordsTable+" WHERE banned = '"+ip+"'");
				if(result2.next())
					plugin.getServer().unbanIP(ip);
				result2.close();
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	public void isMutedThenAdd(String user) {
		ResultSet result = localConn.query("SELECT mute_reason, mute_expires_on, muted_by FROM "+plugin.localMutesTable+" WHERE muted = '"+user+"'");
		
		try {
			if(result.next()) {
				String reason = plugin.viewReason(result.getString("mute_reason"));
				String by = result.getString("muted_by");
				long length = result.getLong("mute_expires_on");
				
				result.close();
				
				if(length != 0) {
					if((System.currentTimeMillis() / 1000) > length ) {
						// Removes them from the database and the HashMap
						plugin.removeMute(user);
						return;
					}
				}
				
				plugin.addMute(user, reason, by, length);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isMuted(String username) {	
		boolean muted = false;
		
		ResultSet result = localConn.query("SELECT mute_id FROM "+plugin.localMutesTable+" WHERE muted = '"+username+"'");
		try {
			if(result.next())
				muted = true;
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return muted;
	}
	
	public MuteData getCurrentMute(String username) {
		
		ResultSet result = localConn.query("SELECT mute_id, mute_reason, muted_by, mute_time, mute_expires_on FROM "+plugin.localMutesTable+" WHERE muteed = '"+username+"'");
		try {
			if(result.next()) {
				
				MuteData data = new MuteData(username, result.getLong("mute_expires_on"), result.getString("mute_reason"), result.getLong("mute_time"), result.getString("muted_by"));
				
				result.close();
				
				return data;
			}
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getCurrentBanInfo(String user) {
		String message = "None";
		
		ResultSet result = localConn.query("SELECT ban_reason, ban_time, ban_expires_on, banned_by, server FROM "+bansTable+" WHERE banned = '"+user+"'");
		try {
			if(result.next()) {
				message = plugin.viewReason(result.getString("ban_reason"))+"\n"+ChatColor.RED+"Banned By: "+result.getString("banned_by");
				String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (result.getLong("ban_time")*1000));
				message += "\n"+ChatColor.RED+"Banned at: "+date;
				long expires = result.getInt("ban_expires_on");
				if(expires == 0)
					message += "\n"+ChatColor.RED+"Expires: Never";
				else {
					// Temp ban, check to see if expired
					long timestampNow = System.currentTimeMillis()/1000;
					if(timestampNow < expires) {
						// Still banned
						expires = (long) expires * 1000;
						message += "\n"+ChatColor.RED+"Expires in: "+plugin.formatDateDiff(expires);
					} else
						message += "\n"+ChatColor.RED+"Expires in: Now";
				}
				String server = result.getString("server");
				if(!server.isEmpty())
					message += "\n"+ChatColor.RED+"Server: "+server;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	public String getCurrentMuteInfo(String user) {
		String message = "None";
		
		ResultSet result = localConn.query("SELECT mute_reason, mute_time, mute_expires_on, muted_by, server FROM "+plugin.localMutesTable+" WHERE muted = '"+user+"'");
		try {
			if(result.next()) {
				message = plugin.viewReason(result.getString("mute_reason"))+"\n"+ChatColor.RED+"Muted By: "+result.getString("muted_by");
				String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (result.getLong("mute_time")*1000));
				message += "\n"+ChatColor.RED+"Muted at: "+date;
				long expires = result.getInt("mute_expires_on");
				if(expires == 0)
					message += "\n"+ChatColor.RED+"Expires: Never";
				else {
					// Temp ban, check to see if expired
					long timestampNow = System.currentTimeMillis()/1000;
					if(timestampNow < expires) {
						// Still banned
						expires = (long) expires * 1000;
						message += "\n"+ChatColor.RED+"Expires in: "+plugin.formatDateDiff(expires);
					} else
						message += "\n"+ChatColor.RED+"Expires in: Now";
				}
				String server = result.getString("server");
				if(!server.isEmpty())
					message += "\n"+ChatColor.RED+"Server: "+server;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return message;
	}
	
	public int getPastBanCount(String user) {
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM "+recordsTable+" WHERE banned = '"+user+"'");
		int count = 0;
		try {
			if(result.next())
				count = result.getInt("numb");
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return count;
	}
	
	public int getPastMuteCount(String user) {
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM "+plugin.localMutesRecordTable+" WHERE muted = '"+user+"'");
		int count = 0;
		try {
			if(result.next())
				count = result.getInt("numb");
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return count;
	}
	
	private void banRemove(int id, String by) {
		// First copy it into ban records
		localConn.query("INSERT INTO "+recordsTable+" (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, \""+by+"\", UNIX_TIMESTAMP(now()), b.server FROM "+bansTable+" b WHERE b.ban_id = '"+id+"'");
		// Now delete it
		localConn.query("DELETE FROM "+bansTable+" WHERE ban_id = '"+id+"'");
	}
	
	public void banRemove(String name, String by) {
		localConn.query("INSERT INTO "+recordsTable+" (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, \""+by+"\", UNIX_TIMESTAMP(now()), b.server FROM "+bansTable+" b WHERE b.banned = '"+name+"'");
		// Now delete it
		localConn.query("DELETE FROM "+bansTable+" WHERE banned = '"+name+"'");
	}
	
	public void ipRemove(String ip, String by) {
		localConn.query("INSERT INTO "+ipRecordsTable+" (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, \""+by+"\", UNIX_TIMESTAMP(now()), b.server FROM "+ipBansTable+" b WHERE b.banned = '"+ip+"'");
		// Now delete it
		localConn.query("DELETE FROM "+ipBansTable+" WHERE banned = '"+ip+"'");
	}
	
	public void muteRemove(String name, String by) {
		localConn.query("INSERT INTO "+plugin.localMutesRecordTable+" (muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time, server) SELECT b.muted, b.muted_by, b.mute_reason, b.mute_time, b.mute_expires_on, \""+by+"\", UNIX_TIMESTAMP(now()), b.server FROM "+plugin.localMutesTable+" b WHERE b.muted = '"+name+"'");
		// Now delete it
		localConn.query("DELETE FROM "+plugin.localMutesTable+" WHERE muted = '"+name+"'");
	}

	public boolean playerInTable(String player) {
		ResultSet result = localConn.query("SELECT banned FROM "+bansTable+" WHERE banned = '"+player+"'");
		try {
			if(result.next()) {
				result.close();
				return true;
			} else
				result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public boolean ipInTable(String ip) {
		ResultSet result = localConn.query("SELECT banned FROM "+ipBansTable+" WHERE banned = '"+ip+"'");
		try {
			if(result.next()) {
				result.close();
				return true;
			} else
				result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void create_tables() throws SQLException {
		boolean Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+bansTable+" ("+
		 "ban_id int(255) NOT NULL AUTO_INCREMENT,"+
		 "banned varchar(32) NOT NULL,"+
		 "banned_by varchar(32) NOT NULL,"+
		 "ban_reason text NOT NULL,"+
		 "ban_time int(10) NOT NULL,"+
		 "ban_expires_on int(10) NOT NULL,"+
		 "server varchar(30) NOT NULL,"+
		 "PRIMARY KEY (ban_id),"+
		 "KEY `banned` (`banned`)"+
		") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
		
		if(!Table)
			plugin.logger.severe("Unable to create local BanManagement table");
		else {
			Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+recordsTable+" ("+
			 "ban_record_id int(255) NOT NULL AUTO_INCREMENT,"+
			 "banned varchar(32) NOT NULL,"+
			 "banned_by varchar(32) NOT NULL,"+
			 "ban_reason text NOT NULL,"+
			 "ban_time int(10) NOT NULL,"+
			 "ban_expired_on int(10) NOT NULL,"+
			 "unbanned_by varchar(32) NOT NULL,"+
			 "unbanned_time int(10) NOT NULL,"+
			 "server varchar(30) NOT NULL,"+
			 "PRIMARY KEY (ban_record_id),"+
			 "KEY `banned` (`banned`)"+
			") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
					
			if(!Table)
				plugin.logger.severe("Unable to create local BanManagement table");
			else {
				Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+ipBansTable+" ("+
				 "ban_id int(255) NOT NULL AUTO_INCREMENT,"+
				 "banned varchar(32) NOT NULL,"+
				 "banned_by varchar(32) NOT NULL,"+
				 "ban_reason text NOT NULL,"+
				 "ban_time int(10) NOT NULL,"+
				 "ban_expires_on int(10) NOT NULL,"+
				 "server varchar(30) NOT NULL,"+
				 "PRIMARY KEY (ban_id),"+
				 "KEY `banned` (`banned`)"+
				") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
				
				if(!Table)
					plugin.logger.severe("Unable to create local BanManagement table");
				else {
					Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+ipRecordsTable+" ("+
					 "ban_record_id int(255) NOT NULL AUTO_INCREMENT,"+
					 "banned varchar(32) NOT NULL,"+
					 "banned_by varchar(32) NOT NULL,"+
					 "ban_reason text NOT NULL,"+
					 "ban_time int(10) NOT NULL,"+
					 "ban_expired_on int(10) NOT NULL,"+
					 "unbanned_by varchar(32) NOT NULL,"+
					 "unbanned_time int(10) NOT NULL,"+
					 "server varchar(30) NOT NULL,"+
					 "PRIMARY KEY (ban_record_id),"+
					 "KEY `banned` (`banned`)"+
					") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
					
					if(!Table)
						plugin.logger.severe("Unable to create local BanManagement table");
					else {
						Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+plugin.localKicksTable+" ("+
						 "kick_id int(255) NOT NULL AUTO_INCREMENT,"+
						 "kicked varchar(32) NOT NULL,"+
						 "kicked_by varchar(32) NOT NULL,"+
						 "kick_reason text NOT NULL,"+
						 "kick_time int(10) NOT NULL,"+
						 "server varchar(30) NOT NULL,"+
						 "PRIMARY KEY (kick_id),"+
						 "KEY `kicked` (`kicked`)"+
						") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
						if(!Table)
							plugin.logger.severe("Unable to create local BanManagement table");
						else {
							Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+plugin.localMutesTable+" ("+
									 "mute_id int(255) NOT NULL AUTO_INCREMENT,"+
									 "muted varchar(32) NOT NULL,"+
									 "muted_by varchar(32) NOT NULL,"+
									 "mute_reason text NOT NULL,"+
									 "mute_time int(10) NOT NULL,"+
									 "mute_expires_on int(10) NOT NULL,"+
									 "server varchar(30) NOT NULL,"+
									 "PRIMARY KEY (mute_id),"+
									 "KEY `muted` (`muted`)"+
									") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
									
									if(!Table)
										plugin.logger.severe("Unable to create local BanManagement table");
									else {
										Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+plugin.localMutesRecordTable+" ("+
										 "mute_record_id int(255) NOT NULL AUTO_INCREMENT,"+
										 "muted varchar(32) NOT NULL,"+
										 "muted_by varchar(32) NOT NULL,"+
										 "mute_reason text NOT NULL,"+
										 "mute_time int(10) NOT NULL,"+
										 "mute_expired_on int(10) NOT NULL,"+
										 "unmuted_by varchar(32) NOT NULL,"+
										 "unmuted_time int(10) NOT NULL,"+
										 "server varchar(30) NOT NULL,"+
										 "PRIMARY KEY (mute_record_id),"+
										 "KEY `muted` (`muted`)"+
										") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
										
										if(!Table)
											plugin.logger.severe("Unable to create local BanManagement table");
										else {
											Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+plugin.localPlayerIpsTable+" ("+
											 "`player` varchar(25) NOT NULL,"+
											 "`ip` int UNSIGNED NOT NULL," +
											 "`last_seen` int(10) NOT NULL,"+
											 "PRIMARY KEY `player` (`player`),"+
											 "KEY `ip` (`ip`)"+
											") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
											
											if(!Table)
												plugin.logger.severe("Unable to create local BanManagement table");
										}
									}
						}
					}
				}
			}
		}
	}
	
	public boolean ipBanned(String ip) {
		Set<String> bans = plugin.getServer().getIPBans();
		if(bans.contains(ip))
			return true;
		return false;
	}
	
	public void setIP(String name, InetAddress ip) {
		plugin.asyncQuery("INSERT INTO "+plugin.localPlayerIpsTable+" (`player`, `ip`, `last_seen`) VALUES ('" + name + "', INET_ATON('"+plugin.getIp(ip)+"'), '"+System.currentTimeMillis() / 1000+"') ON DUPLICATE KEY UPDATE ip = INET_ATON('"+plugin.getIp(ip)+"'), last_seen = '"+System.currentTimeMillis() / 1000+"'");
	}
	
	public String getIP(String name) {
		String ip = "";
		
		ResultSet result = localConn.query("SELECT INET_NTOA(ip) AS ipAddress FROM "+plugin.localPlayerIpsTable+" WHERE player = '"+name+"'");
		
		try {
			if(result.next())
				ip = result.getString("ipAddress");
			result.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ip;
	}

	public void closeConnection() {
		localConn.close();		
	}
	
	public void serverExists() {
		if(!localConn.colExists(bansTable, "server")) {
			// Hmm, they don't exist, lets add them!
			localConn.query("ALTER TABLE "+bansTable+" ADD server VARCHAR(30) NOT NULL");
			localConn.query("ALTER TABLE "+recordsTable+" ADD server VARCHAR(30) NOT NULL");
			localConn.query("ALTER TABLE "+ipBansTable+" ADD server VARCHAR(30) NOT NULL");
			localConn.query("ALTER TABLE "+ipRecordsTable+" ADD server VARCHAR(30) NOT NULL");
			localConn.query("ALTER TABLE "+plugin.localKicksTable+" ADD server VARCHAR(30) NOT NULL");
		}
	}
}