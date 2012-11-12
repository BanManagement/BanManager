package me.confuserr.banmanager.listeners;

import me.confuserr.banmanager.BanManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotify implements Listener {
	
	private BanManager plugin;

	public UpdateNotify(BanManager instance) {
		plugin = instance;
	}

	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(plugin.updateAvailable) {
			final Player player = event.getPlayer();
			if(player.hasPermission("bm.update")) {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				   public void run() {
					   player.sendMessage(plugin.banMessages.get("commandPermissionError").replace("[version]", plugin.updateVersion));
					   player.sendMessage(ChatColor.GOLD+"http://dev.bukkit.org/server-mods/ban-management/");
				   }
				}, 40L);
			}
		}
	}

}
