package me.confuserr.banmanager.scheduler;

import java.sql.ResultSet;
import java.sql.SQLException;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Database;

public class muteAsync implements Runnable {
	
	private Database localConn;
	private BanManager plugin;
	private long lastRun;

	public muteAsync(BanManager banManager) {
		plugin = banManager;
		localConn = plugin.localConn;
		lastRun = System.currentTimeMillis() / 1000;
	}

	@Override
	public void run() {
		// Check for new mutes
		ResultSet result = localConn.query("SELECT * FROM "+localConn.mutesTable+" WHERE mute_time > "+lastRun+"");
		
		long now = System.currentTimeMillis() / 1000;
		
		try {
			while(result.next()) {
				// Add them to the muted list
				String player = result.getString("muted");
				String reason = result.getString("mute_reason");
				String by = result.getString("muted_by");
				Long expires = result.getLong("mute_expires_on");
				
				if(expires != 0 && expires < now) {
					plugin.removeMute(player);
					// Remove the mute!
				} else if(!plugin.mutedPlayersBy.containsKey(player)) {
					// Firt we see if they are online, if they are then and only then do we mute them, otherwise we're just adding to the
					// HashMap for no reason
					if(plugin.getServer().getPlayer(player) != null) {
						// Add the mute!
						plugin.addMute(player, reason, by, expires);
					}
				}
			}
			
			result.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// Check for old mutes and remove them!
		ResultSet result1 = localConn.query("SELECT * FROM "+localConn.mutesRecordTable+" WHERE mute_time > "+lastRun+"");
		
		try {
			while(result1.next()) {
				// Remove them from the muted list
				String player = result1.getString("muted");
				Long expires = result1.getLong("mute_expired_on");
				
				if(plugin.mutedPlayersBy.containsKey(player)) {
					if(plugin.mutedPlayersLength.get(player) == expires)
						plugin.removeHashMute(player);
				}
			}
			
			result1.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		lastRun = System.currentTimeMillis() / 1000;
	}
}
