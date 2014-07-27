package me.confuser.banmanager.runnables;

import java.sql.SQLException;

import org.bukkit.entity.Player;

import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.bukkitutil.Message;

public class BanSync implements Runnable {
	private BanManager plugin = BanManager.getPlugin();
	private PlayerBanStorage banStorage = plugin.getPlayerBanStorage();
	private long lastChecked = 0;

	public BanSync() {
		
	}
	
	@Override
	public void run() {
		// New/updated bans check
		newBansSync();
	}

	private void newBansSync() throws SQLException {
		QueryBuilder<PlayerBanData, byte[]> query = banStorage.queryBuilder();
		Where<PlayerBanData, byte[]> where = query.where();
		
		where
			.ge("created", lastChecked)
			.or()
			.ge("updated", lastChecked);
		
		query.setWhere(where);
		CloseableIterator<PlayerBanData> itr = query.iterator();
		
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
}
