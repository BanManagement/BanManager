package me.confuserr.banmanager.listeners;

import java.net.InetAddress;

import me.confuserr.banmanager.BanManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPreLogin implements Listener {
	
	private BanManager plugin;

	public AsyncPreLogin(BanManager instance) {
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(final AsyncPlayerPreLoginEvent event) {
		String name = event.getName();
		InetAddress ip = event.getAddress();
		
		// Check to see if they are to be unbanned
		if(plugin.toUnbanPlayer.contains(name)) {
			// Ok they are, no need to do additional checks!
			// But we unban them now otherwise they'll get the Ban Hammer message
			// This is experimental, as its an async event, shouldn't really be calling a main thread method, shall see how it goes for now!
			plugin.getServer().getOfflinePlayer(name).setBanned(false);
			return;
		}
		
		// Do the same as above but for IP
		if(plugin.toUnbanIp.contains(ip)) {
			// But we unban them now otherwise they'll get the Ban Hammer message
			plugin.getServer().unbanIP(plugin.getIp(ip));
			return;
		}
		
		String banReason = plugin.dbLogger.isBanned(name);
		if(!banReason.isEmpty()) {
			banReason = BanManager.colorize(banReason);
			// Oh dear, they've been banned
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banReason);
			return;
		} else {
			String ipReason = plugin.dbLogger.isBanned(ip);
			if(!ipReason.isEmpty()) {
				ipReason = BanManager.colorize(ipReason);
				// Oh dear, they've been banned
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ipReason);
				return;
			}
		}
		
		// Here we check to see if player is muted, if they are, we add them to the list!
		if(!plugin.mutedPlayersBy.containsKey(name)) {
			plugin.dbLogger.isMutedThenAdd(name);
		}
		
		// Here we log their IP to the database
		// plugin.dbLogger.setIp(name, ip);
	}
}
