package me.confuser.banmanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.listeners.Listeners;

public class LeaveListener extends Listeners<BanManager> {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onLeave(PlayerQuitEvent event) {
		plugin.getPlayerStorage().removeOnline(event.getPlayer().getUniqueId());
	}
}
