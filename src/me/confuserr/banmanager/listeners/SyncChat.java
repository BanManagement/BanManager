package me.confuserr.banmanager.listeners;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings("deprecation")
public class SyncChat implements Listener {
	
	private BanManager plugin;

	public SyncChat(BanManager instance) {
		plugin = instance;
	}

	@EventHandler
	public void onPlayerChat(final PlayerChatEvent event) {
		Player player = event.getPlayer();
		String playerName = player.getName();
		
		if(plugin.mutedPlayersBy.containsKey(playerName)) {

			long time = plugin.mutedPlayersLength.get(playerName);
			String reason = Util.viewReason(plugin.mutedPlayersReason.get(playerName));
			String by = plugin.mutedPlayersBy.get(playerName);
			String expires = plugin.formatDateDiff(time);
			
			if(time != 0) {
				if(System.currentTimeMillis() < time ) {
					event.setCancelled(true);
					String mutedMessage = plugin.banMessages.get("tempMuted").replace("[expires]", expires).replace("[reason]", reason).replace("[by]", by);
					player.sendMessage(mutedMessage);
				} else {
					// Removes them from the database and the HashMap
					player.sendMessage("Unmuted!");
					plugin.removeMute(playerName);
				}
			} else {
				event.setCancelled(true);
				String mutedMessage = plugin.banMessages.get("muted").replace("[reason]", reason).replace("[by]", by);
				player.sendMessage(mutedMessage);
			}
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		String playerName = event.getPlayer().getName();
		
		if(plugin.mutedPlayersBy.containsKey(playerName))
			plugin.removeHashMute(playerName);
	}
}
