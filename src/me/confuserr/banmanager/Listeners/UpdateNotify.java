package me.confuserr.banmanager.Listeners;

import me.confuserr.banmanager.BanManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotify implements Listener {
	
	private final BanManager plugin;

	public UpdateNotify(BanManager instance) {
		plugin = instance;
	}

	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(plugin.isUpdateAvailable()) {
			final Player player = event.getPlayer();
			if(player.hasPermission("bm.update")) {
				plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
				   public void run() {
					   player.sendMessage(plugin.getMessage("updateAvailable").replace("[version]", plugin.updateVersion));
					   player.sendMessage(ChatColor.GOLD+"http://dev.bukkit.org/server-mods/ban-management/");
				   }
				}, 40L);
			}
		}
	}

}
