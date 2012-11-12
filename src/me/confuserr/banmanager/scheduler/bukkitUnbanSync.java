package me.confuserr.banmanager.scheduler;

import java.util.Iterator;

import org.bukkit.Server;

import me.confuserr.banmanager.BanManager;

public class bukkitUnbanSync implements Runnable {
	
	private BanManager plugin;

	public bukkitUnbanSync(BanManager banManager) {
		plugin = banManager;
	}

	@Override
	public void run() {
		Server server = plugin.getServer();
		if(plugin.toUnbanPlayer.size() > 0) {
			synchronized(plugin.toUnbanPlayer) {
				Iterator<String> itr = plugin.toUnbanPlayer.iterator();
				while(itr.hasNext()) {
					String p = itr.next();
					server.getOfflinePlayer(p).setBanned(false);
					itr.remove();
				}
			}
		}
		
		if(plugin.toUnbanIp.size() > 0) {
			synchronized(plugin.toUnbanIp) {
				Iterator<String> itr = plugin.toUnbanIp.iterator();
				while(itr.hasNext()) {
					String p = itr.next();
					server.unbanIP(p);
					itr.remove();
				}
			}
		}		
	}

}
