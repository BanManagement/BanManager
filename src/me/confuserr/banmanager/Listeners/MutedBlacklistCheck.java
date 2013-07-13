package me.confuserr.banmanager.Listeners;

import me.confuserr.banmanager.BanManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MutedBlacklistCheck implements Listener {
	private BanManager plugin;

	public MutedBlacklistCheck(BanManager instance) {
		plugin = instance;
	}

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (!plugin.isPlayerMutedInMem(event.getPlayer().getName()))
			return;

		// Split the command
		String[] args = event.getMessage().split(" ");

		// Get rid of the first /
		String cmd = args[0].replace("/", "");

		// Check to see if its blacklisted
		if (plugin.getMutedBlacklist().contains(cmd)) {
			// Cancel it
			event.setCancelled(true);
			event.getPlayer().sendMessage(plugin.getMessage("mutedBlacklistedCommand"));
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.getPlayerMutes().remove(event.getPlayer().getName());
	}
}
