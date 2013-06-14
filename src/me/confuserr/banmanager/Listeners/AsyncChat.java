package me.confuserr.banmanager.Listeners;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;
import me.confuserr.banmanager.data.MuteData;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChat implements Listener {
	
	private BanManager plugin;

	public AsyncChat(BanManager instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerChat(final AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if(plugin.getPlayerMutes().get(playerName) != null) {

			MuteData muteData = plugin.getPlayerMutes().get(playerName);
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
