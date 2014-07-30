package me.confuser.banmanager.listeners;

import java.sql.SQLException;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;

public class ChatListener extends Listeners<BanManager> {

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (!plugin.getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId()))
			return;
		
		PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(event.getPlayer().getUniqueId());
		
		if (mute.hasExpired()) {
			try {
				plugin.getPlayerMuteStorage().unmute(mute, plugin.getPlayerStorage().getConsole());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		}
		
		event.setCancelled(true);
		
		Message message;
		
		if (mute.getExpires() == 0) {
			message = Message.get("muted")
				.set("displayName", event.getPlayer().getDisplayName())
				.set("player", event.getPlayer().getName())
				.set("reason", mute.getReason())
				.set("actor", mute.getActor().getName());
		} else {
			message = Message.get("tempMuted")
				.set("displayName", event.getPlayer().getDisplayName())
				.set("player", event.getPlayer().getName())
				.set("reason", mute.getReason())
				.set("actor", mute.getActor().getName())
				.set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
		}
		
		event.getPlayer().sendMessage(message.toString());
	}
}
