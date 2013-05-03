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
	private BanManager plugin;
	
	DbLogger(Database conn, BanManager instance) {
		localConn = conn;
		plugin = instance;
	}
	
	public void logBan(String banned, String banned_by, String reason) {
		banned = banned.toLowerCase();
		
		Util.asyncQuery("INSERT INTO "+localConn.bansTable+" (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '0', '"+plugin.serverName+"')");
		plugin.bannedPlayers.add(banned);
	}
	
	public void logTempBan(String banned, String banned_by, String reason, long expires) {
		banned = banned.toLowerCase();
		
		Util.asyncQuery("INSERT INTO "+localConn.bansTable+" (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+expires+"', '"+plugin.serverName+"')");
		plugin.bannedPlayers.add(banned);
	}
	
	public void logIpBan(String banned, String banned_by, String reason) {
		Util.asyncQuery("INSERT INTO "+localConn.ipBansTable+" (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '0', '"+plugin.serverName+"')");
		plugin.bannedIps.add(banned);
	}
	
	public void logTempIpBan(String banned, String banned_by, String reason, long expires) {
		Util.asyncQuery("INSERT INTO "+localConn.ipBansTable+" (banned, banned_by, ban_reason, ban_time, ban_expires_on, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+expires+"', '"+plugin.serverName+"')");
		plugin.bannedIps.add(banned);
	}
	
	public void logKick(String banned, String banned_by, String reason) {
		Util.asyncQuery("INSERT INTO "+localConn.kicksTable+" (kicked, kicked_by, kick_reason, kick_time, server) VALUES ('"+banned+"', '"+banned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+plugin.serverName+"')");
	}
	
	public void logMute(String muted, String muted_by, String reason) {
		Util.asyncQuery("INSERT INTO "+localConn.mutesTable+" (muted, muted_by, mute_reason, mute_time, mute_expires_on, server) VALUES ('"+muted+"', '"+muted_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '0', '"+plugin.serverName+"')");
	}
	
	public void logTempMute(String muted, String muted_by, String reason, long expires) {
		Util.asyncQuery("INSERT INTO "+localConn.mutesTable+" (muted, muted_by, mute_reason, mute_time, mute_expires_on, server) VALUES ('"+muted+"', '"+muted_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+expires+"', '"+plugin.serverName+"')");
	}
	
	public void logWarning(String warned, String warned_by, String reason) {
		Util.asyncQuery("INSERT INTO "+localConn.warningsTable+" (warned, warned_by, warn_reason, warn_time, server) VALUES ('"+warned+"', '"+warned_by+"', '"+reason+"', UNIX_TIMESTAMP(now()), '"+plugin.serverName+"')");
	}
	
	public BanData getCurrentBan(String username) {
		
		ResultSet result = localConn.query("SELECT ban_id, ban_reason, banned_by, ban_time, ban_expires_on FROM "+localConn.bansTable+" WHERE banned = '"+username+"'");
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
		
		ResultSet result = localConn.query("SELECT ban_id, ban_reason, banned_by, ban_time, ban_expires_on FROM "+localConn.bansTable+" WHERE banned = '"+username.toLowerCase()+"'");
		try {
			if(result.next()) {
				// Found, check to see if perma banned
				// But first, we see if they are bukkit banned, if not we make it so
				if(plugin.bukkitBan) {
					if(!plugin.getServer().getOfflinePlayer(username).isBanned()) {
						plugin.getServer().getOfflinePlayer(username).setBanned(true);
					}
				}
				long expires = result.getLong("ban_expires_on");
				String reason = Util.viewReason(result.getString("ban_reason"));
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

						banRemove(username, "Console automated");
					}
				}
			} else if(plugin.getServer().getOfflinePlayer(username).isBanned() && plugin.bukkitBan) {
				// Not in the current bans, but they are banned by bukkit
				// Check if they've been previously banned, if they have, unban them
				// Not unbanning without this check in case they were banned before the plugin was installed
				ResultSet result2 = localConn.query("SELECT banned FROM "+localConn.bansRecordTable+" WHERE banned = '"+username.toLowerCase()+"'");
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
		
		ResultSet result = localConn.query("SELECT ban_id, ban_reason, banned_by, ban_time, ban_expires_on FROM "+localConn.ipBansTable+" WHERE banned = '"+ip+"'");
		try {
			if(result.next()) {
				// Found, check to see if perma banned
				// But first, we see if they are bukkit banned, if not we make it so
				if(plugin.bukkitBan) {
					if(!ipBanned(ip))
						plugin.getServer().banIP(ip);
				}
				
				long expires = result.getLong("ban_expires_on");
				String reason = Util.viewReason(result.getString("ban_reason"));
				String by = result.getString("banned_by");
				
				if(expires == 0) {
					// Perma banned
					message = plugin.banMessages.get("disconnectIpBan").replace("[ip]", ip).replace("[reason]", reason).replace("[by]", by);
				} else {
					// Temp ban, check to see if expired
					long timestampNow = System.currentTimeMillis()/1000;
					if(timestampNow < expires) {
						// Still banned
						expires = (long) expires * 1000;
						String formatExpires = plugin.formatDateDiff(expires);
						message = plugin.banMessages.get("disconnectTempIpBan").replace("[ip]", ip).replace("[expires]", formatExpires).replace("[reason]", reason).replace("[by]", by);
					} else {
						// No longer banned, remove the ban!
						if(plugin.bukkitBan)
							plugin.getServer().unbanIP(ip);

						ipRemove(ip, "Console automated");
					}
				}
			} else if(ipBanned(ip) && plugin.bukkitBan) {
				// Not in the current bans, but they are banned by bukkit
				// Check if they've been previously banned, if they have, unban them
				// Not unbanning without this check in case they were banned before the plugin was installed
				ResultSet result2 = localConn.query("SELECT banned FROM "+localConn.ipBansRecordTable+" WHERE banned = '"+ip+"'");
				
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
		ResultSet result = localConn.query("SELECT mute_reason, mute_expires_on, muted_by FROM "+localConn.mutesTable+" WHERE muted = '"+user+"'");
		
		try {
			if(result.next()) {
				String reason = Util.viewReason(result.getString("mute_reason"));
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
		
		ResultSet result = localConn.query("SELECT mute_id FROM "+localConn.mutesTable+" WHERE muted = '"+username+"'");
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
		
		ResultSet result = localConn.query("SELECT mute_id, mute_reason, muted_by, mute_time, mute_expires_on FROM "+localConn.mutesTable+" WHERE muteed = '"+username+"'");
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
		
		ResultSet result = localConn.query("SELECT ban_reason, ban_time, ban_expires_on, banned_by, server FROM "+localConn.bansTable+" WHERE banned = '"+user+"'");
		try {
			if(result.next()) {
				message = Util.viewReason(result.getString("ban_reason"))+"\n"+ChatColor.RED+"Banned By: "+result.getString("banned_by");
				String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (result.getLong("ban_time")*1000));
				message += "\n"+ChatColor.RED+"Banned at: "+date;
				long expires = result.getLong("ban_expires_on");
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
		
		ResultSet result = localConn.query("SELECT mute_reason, mute_time, mute_expires_on, muted_by, server FROM "+localConn.mutesTable+" WHERE muted = '"+user+"'");
		try {
			if(result.next()) {
				message = Util.viewReason(result.getString("mute_reason"))+"\n"+ChatColor.RED+"Muted By: "+result.getString("muted_by");
				String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date (result.getLong("mute_time")*1000));
				message += "\n"+ChatColor.RED+"Muted at: "+date;
				long expires = result.getLong("mute_expires_on");
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
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM "+localConn.bansRecordTable+" WHERE banned = '"+user+"'");
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
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM "+localConn.mutesRecordTable+" WHERE muted = '"+user+"'");
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
	
	public int getWarningCount(String user) {
		ResultSet result = localConn.query("SELECT COUNT(*) AS numb FROM "+localConn.warningsTable+" WHERE warned = '"+user+"'");
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
	
	public void banRemove(String name, String by) {
		name = name.toLowerCase();
		
		Util.asyncQuery("INSERT INTO "+localConn.bansRecordTable+" (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, \""+by+"\", UNIX_TIMESTAMP(now()), b.server FROM "+localConn.bansTable+" b WHERE b.banned = '"+name+"'");
		// Now delete it
		Util.asyncQuery("DELETE FROM "+localConn.bansTable+" WHERE banned = '"+name+"'");
		plugin.bannedPlayers.remove(name);
	}

	public void ipRemove(String ip, String by) {
		Util.asyncQuery("INSERT INTO "+localConn.ipBansRecordTable+" (banned, banned_by, ban_reason, ban_time, ban_expired_on, unbanned_by, unbanned_time, server) SELECT b.banned, b.banned_by, b.ban_reason, b.ban_time, b.ban_expires_on, \""+by+"\", UNIX_TIMESTAMP(now()), b.server FROM "+localConn.ipBansTable+" b WHERE b.banned = '"+ip+"'");
		// Now delete it
		Util.asyncQuery("DELETE FROM "+localConn.ipBansTable+" WHERE banned = '"+ip+"'");
		plugin.bannedIps.remove(ip);
	}
	
	public void muteRemove(String name, String by) {
		Util.asyncQuery("INSERT INTO "+localConn.mutesRecordTable+" (muted, muted_by, mute_reason, mute_time, mute_expired_on, unmuted_by, unmuted_time, server) SELECT b.muted, b.muted_by, b.mute_reason, b.mute_time, b.mute_expires_on, \""+by+"\", UNIX_TIMESTAMP(now()), b.server FROM "+localConn.mutesTable+" b WHERE b.muted = '"+name+"'");
		// Now delete it
		Util.asyncQuery("DELETE FROM "+localConn.mutesTable+" WHERE muted = '"+name+"'");
	}

	public boolean playerInTable(String player) {
		ResultSet result = localConn.query("SELECT banned FROM "+localConn.bansTable+" WHERE banned = '"+player+"'");
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
		ResultSet result = localConn.query("SELECT banned FROM "+localConn.ipBansTable+" WHERE banned = '"+ip+"'");
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
		boolean Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.bansTable+" ("+
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
			Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.bansRecordTable+" ("+
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
				Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.ipBansTable+" ("+
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
					Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.ipBansRecordTable+" ("+
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
						Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.kicksTable+" ("+
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
							Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.mutesTable+" ("+
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
										Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.mutesRecordTable+" ("+
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
											Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.playerIpsTable+" ("+
											 "`player` varchar(25) NOT NULL,"+
											 "`ip` int UNSIGNED NOT NULL," +
											 "`last_seen` int(10) NOT NULL,"+
											 "PRIMARY KEY `player` (`player`),"+
											 "KEY `ip` (`ip`)"+
											") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
											
											
											if(!Table)
												plugin.logger.severe("Unable to create local BanManagement table");
											else {
												Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.warningsTable+" ("+
													"warn_id int(255) NOT NULL AUTO_INCREMENT,"+
													"warned varchar(32) NOT NULL,"+
													"warned_by varchar(32) NOT NULL,"+
													"warn_reason text NOT NULL,"+
													"warn_time int(10) NOT NULL,"+
													"server varchar(30) NOT NULL,"+
													"PRIMARY KEY (warn_id),"+
													"KEY `kicked` (`warned`)"+
												") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
												
												if(!Table)
													plugin.logger.severe("Unable to create local BanManagement table");
											}

											/*if(!Table)
												plugin.logger.severe("Unable to create local BanManagement table");
											else {
												Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.banAppealsTable+" ("+
												 "`appeal_id` int(255) NOT NULL AUTO_INCREMENT,"+
												 "`ban_id` int(255) NOT NULL," +
												 "`ban_type` int(1) NOT NULL,"+
												 "`appeal_time` int(10) NOT NULL,"+
												 "PRIMARY KEY `player` (`player`),"+
												 "KEY `ip` (`ip`)"+
												") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
												
												if(!Table)
													plugin.logger.severe("Unable to create local BanManagement table");
												else {
													Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.pinsTable+" ("+
													 "`pin_id` int(255) UNSIGNED NOT NULL AUTO_INCREMENT,"+
													 "`player` varchar(25) NOT NULL," +
													 "`ban_type` int(1) NOT NULL,"+
													 "`appeal_time` int(10) NOT NULL,"+
													 "PRIMARY KEY `player` (`player`),"+
													 "KEY `ip` (`ip`)"+
													") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
													
													if(!Table)
														plugin.logger.severe("Unable to create local BanManagement table");
													else {
														Table = localConn.createTable("CREATE TABLE IF NOT EXISTS "+localConn.staffTable+" ("+
														 "`staff_id` int(255) UNSIGNED NOT NULL AUTO_INCREMENT,"+
														 "`ssid` varchar(32) NOT NULL,"+
														 "`player` varchar(25) NOT NULL," +
														 "`permissions` int(255) UNSIGNED NOT NULL,"+
														 "`password_hash` varchar(40) NOT NULL,"+
														 "`password_salt` varchar(10),"+
														 "PRIMARY KEY `staff_id` (`staff_id`),"+
														 "KEY `ssid` (`ssid`)"+
														") ENGINE=MyISAM  DEFAULT CHARSET=latin1");
														
														if(!Table)
															plugin.logger.severe("Unable to create local BanManagement table");
													}
												}
											}*/
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
		Util.asyncQuery("INSERT INTO "+localConn.playerIpsTable+" (`player`, `ip`, `last_seen`) VALUES ('" + name + "', INET_ATON('"+plugin.getIp(ip)+"'), '"+System.currentTimeMillis() / 1000+"') ON DUPLICATE KEY UPDATE ip = INET_ATON('"+plugin.getIp(ip)+"'), last_seen = '"+System.currentTimeMillis() / 1000+"'");
	}
	
	public String getIP(String name) {
		String ip = "";
		
		ResultSet result = localConn.query("SELECT INET_NTOA(ip) AS ipAddress FROM "+localConn.playerIpsTable+" WHERE player = '"+name+"'");
		
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
}