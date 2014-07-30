package me.confuser.banmanager.storage;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.events.PlayerMuteEvent;
import me.confuser.banmanager.events.PlayerUnmuteEvent;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class PlayerMuteStorage  extends BaseDaoImpl<PlayerMuteData, byte[]> {
	private BanManager plugin = BanManager.getPlugin();
	private ConcurrentHashMap<UUID, PlayerMuteData> mutes = new ConcurrentHashMap<UUID, PlayerMuteData>();

	public PlayerMuteStorage(ConnectionSource connection, DatabaseTableConfig<PlayerMuteData> tableConfig) throws SQLException {
		super(connection, tableConfig);
		
		CloseableIterator<PlayerMuteData> itr = iterator();
		
		while(itr.hasNext()) {
			PlayerMuteData mute = itr.next();
			
			mutes.put(mute.getUUID(), mute);
		}
	}
	
	public ConcurrentHashMap<UUID, PlayerMuteData> getMutes() {
		return mutes;
	}
	
	public boolean isMuted(UUID uuid) {
		return mutes.get(uuid) != null;
	}
	
	public boolean isMuted(String playerName) {
		for (PlayerMuteData mute : mutes.values()) {
			if (mute.getPlayer().getName().equalsIgnoreCase(playerName))
				return true;
		}
		
		return false;
	}

	public PlayerMuteData getMute(UUID uuid) {
		return mutes.get(uuid);
	}
	
	public PlayerMuteData getMute(String playerName) {
		for (PlayerMuteData mute : mutes.values()) {
			if (mute.getPlayer().getName().equalsIgnoreCase(playerName))
				return mute;
		}
		
		return null;
	}
	
	public boolean mute(PlayerMuteData mute) throws SQLException {
		PlayerMuteEvent event = new PlayerMuteEvent(mute);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return false;
		
		create(mute);
		mutes.put(mute.getUUID(), mute);
		
		return true;
	}
	
	public boolean unmute(PlayerMuteData mute, PlayerData actor) throws SQLException {
		PlayerUnmuteEvent event = new PlayerUnmuteEvent(mute);
		Bukkit.getServer().getPluginManager().callEvent(event);
		
		if (event.isCancelled())
			return false;

		delete(mute);
		mutes.remove(mute.getUUID());
		
		plugin.getPlayerMuteRecordStorage().addRecord(mute, actor);
		
		return true;
	}
}
