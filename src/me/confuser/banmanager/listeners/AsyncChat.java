package me.confuser.banmanager.listeners;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.Util;
import me.confuser.banmanager.data.MuteData;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChat implements Listener {
	
	private BanManager plugin;

	public AsyncChat(BanManager instance) {
		plugin = instance;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if(plugin.isPlayerMutedInMem(playerName)) {

			MuteData muteData = plugin.getPlayerMuteFromMem(playerName);
			long expires = muteData.getExpires() * 1000;
			String expiresFormat = Util.formatDateDiff(expires);
			
			if(muteData.getExpires() != 0) {
				if(System.currentTimeMillis() < expires) {
					event.setCancelled(true);
					String mutedMessage = plugin.getMessage("tempMuted").replace("[expires]", expiresFormat).replace("[reason]", muteData.getReason()).replace("[by]", muteData.getBy());
					player.sendMessage(mutedMessage);
				} else {
					// Removes them from the database and the HashMap
					player.sendMessage("Unmuted!");
					plugin.removePlayerMute(playerName, plugin.getMessage("consoleName"), true);
				}
			} else {
				event.setCancelled(true);
				String mutedMessage = plugin.getMessage("muted").replace("[reason]", muteData.getReason()).replace("[by]", muteData.getBy());
				player.sendMessage(mutedMessage);
			}
		}
	}
}
