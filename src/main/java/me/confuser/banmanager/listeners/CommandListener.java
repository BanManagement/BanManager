package me.confuser.banmanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;

public class CommandListener extends Listeners<BanManager> {

	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (!plugin.getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId()))
			return;

		// Split the command
		String[] args = event.getMessage().split(" ");

		// Get rid of the first /
		String cmd = args[0].replace("/", "").toLowerCase();

		if (!plugin.getDefaultConfig().isBlockedCommand(cmd))
			return;
		
		event.setCancelled(true);
		event.getPlayer().sendMessage(Message.get("mutedBlacklistedCommand").set("command", cmd).toString());
	}
}
