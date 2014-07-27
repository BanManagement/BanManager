package me.confuser.banmanager.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.events.PlayerBanEvent;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;

public class ConsoleListener extends Listeners<BanManager> {
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBan(PlayerBanEvent event) {
		PlayerBanData ban = event.getBan();
		Message message = Message.get("consoleBanInfo");
		
		message
			.set("player", ban.getPlayer().getName())
			.set("actor", ban.getActor().getName())
			.set("reason", ban.getReason());
		
		if (ban.getExpires() == 0) {
			message.set("expires", Message.get("never").toString());
		} else {
			message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
		}
		
		plugin.getLogger().info(message.toString());
	}
}
