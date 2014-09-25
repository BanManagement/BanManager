package me.confuser.banmanager.runnables;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import java.util.List;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.bukkitutil.Message;

public class BanSync implements Runnable {
	private BanManager plugin = BanManager.getPlugin();
	private PlayerBanStorage banStorage = plugin.getPlayerBanStorage();
	private long lastChecked = 0;

	public BanSync() {
		lastChecked = plugin.getSchedulesConfig().getLastChecked("playerBans");
	}
	
	@Override
	public void run() {
		// New/updated bans check
		try {
			newBans();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		// New unbans
		try {
			newUnbans();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		lastChecked = System.currentTimeMillis() / 1000L;
		plugin.getSchedulesConfig().setLastChecked("playerBans", lastChecked);
	}

	private void newBans() throws SQLException {
		
		List<PlayerBanData> bans = banStorage.findBans(lastChecked);
		
		for (final PlayerBanData ban : bans) {
			if (banStorage.isBanned(ban.getPlayer().getUUID()) && ban.getUpdated() < lastChecked)
				continue;
			
			banStorage.addBan(ban);
			
			if (!plugin.getPlayerStorage().isOnline(ban.getPlayer().getUUID()))
				continue;
			
			plugin.getServer().getScheduler().runTask(plugin, new Runnable(){
				@Override
				public void run() {
					Player bukkitPlayer = plugin.getServer().getPlayer(ban.getPlayer().getUUID());
					
					Message kickMessage = Message.get("banKick")
						.set("displayName", bukkitPlayer.getDisplayName())
						.set("player", ban.getPlayer().getName())
						.set("reason", ban.getReason())
						.set("actor", ban.getActor().getName());
					
					bukkitPlayer.kickPlayer(kickMessage.toString());
				}
			});

		}
		
		bans = null;
	}
	
	private void newUnbans() throws SQLException {
		
		List<PlayerBanRecord> ban_records = plugin.getPlayerBanRecordStorage().findUnbans(lastChecked);
		
		for (PlayerBanRecord ban : ban_records) {
			if (!banStorage.isBanned(ban.getPlayer().getUUID()))
				continue;
			
			banStorage.removeBan(ban.getPlayer().getUUID());

		}
		
		ban_records = null;
	}
}
