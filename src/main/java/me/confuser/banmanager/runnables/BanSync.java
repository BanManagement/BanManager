package me.confuser.banmanager.runnables;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.j256.ormlite.dao.CloseableIterator;
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
		// TODO Set lastChecked from config
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
	}

	private void newBans() throws SQLException {
		
		CloseableIterator<PlayerBanData> itr = banStorage.findBans(lastChecked);
		
		while(itr.hasNext()) {
			final PlayerBanData ban = itr.next();
			
			if (banStorage.isBanned(ban.getUUID()) && ban.getUpdated() < lastChecked)
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
		
		itr.close();
	}
	
	private void newUnbans() throws SQLException {
		
		CloseableIterator<PlayerBanRecord> itr = plugin.getPlayerBanRecordStorage().findUnbans(lastChecked);
		
		while(itr.hasNext()) {
			final PlayerBanRecord ban = itr.next();
			
			if (!banStorage.isBanned(ban.getPlayer().getUUID()))
				continue;
			
			banStorage.removeBan(ban.getPlayer().getUUID());

		}
		
		itr.close();
	}
}
