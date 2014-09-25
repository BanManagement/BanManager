package me.confuser.banmanager.runnables;

import java.sql.SQLException;
import java.util.List;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.PlayerMuteRecord;
import me.confuser.banmanager.storage.PlayerMuteStorage;

public class MuteSync implements Runnable {
	private BanManager plugin = BanManager.getPlugin();
	private PlayerMuteStorage muteStorage = plugin.getPlayerMuteStorage();
	private long lastChecked = 0;

	public MuteSync() {
		lastChecked = plugin.getSchedulesConfig().getLastChecked("playerMutes");
	}
	
	@Override
	public void run() {
		// New/updated mutes check
		try {
			newMutes();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// New unbans
		try {
			newUnmutes();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		lastChecked = System.currentTimeMillis() / 1000L;
		plugin.getSchedulesConfig().setLastChecked("playerMutes", lastChecked);
	}

	private void newMutes() throws SQLException {
		
		List<PlayerMuteData> player_mute_data = muteStorage.findMutes(lastChecked);
		
		for (PlayerMuteData mute : player_mute_data) {
			if (muteStorage.isMuted(mute.getPlayer().getUUID()) && mute.getUpdated() < lastChecked)
				continue;
			
			muteStorage.addMute(mute);

		}
		
		player_mute_data = null;
	}
	
	private void newUnmutes() throws SQLException {
		
		List<PlayerMuteRecord> player_mute_records = plugin.getPlayerMuteRecordStorage().findUnmutes(lastChecked);
		
		for (PlayerMuteRecord mute : player_mute_records) {
			
			if (!muteStorage.isMuted(mute.getPlayer().getUUID()))
				continue;
			
			muteStorage.removeMute(mute.getPlayer().getUUID());

		}
		
		player_mute_records = null;
	}
}
