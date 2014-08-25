package me.confuser.banmanager.listeners;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;

import com.j256.ormlite.dao.CloseableIterator;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.listeners.Listeners;

public class JoinListener extends Listeners<BanManager> {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void banCheck(final AsyncPlayerPreLoginEvent event) {
		if (plugin.getIpBanStorage().isBanned(event.getAddress())) {
			IpBanData data = plugin.getIpBanStorage().getBan(event.getAddress());
			
			if (data.hasExpired()) {
				try {
					plugin.getIpBanStorage().unban(data, plugin.getPlayerStorage().getConsole());
				} catch (SQLException e) {
					e.printStackTrace();
				}
				return;
			}
			
			Message message = null;
			
			if (data.getExpires() == 0) {
				message = Message.get("disallowedIpPermBan");
			} else {
				message = Message.get("disallowedIpTempBan");
				message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
			}
			
			message.set("ip", event.getAddress().toString());
			message.set("reason", data.getReason());
			message.set("actor", data.getActor().getName());
			
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message.toString());
			
			return;
		}
		if (!plugin.getPlayerBanStorage().isBanned(event.getUniqueId())) {
			try {
				plugin.getPlayerStorage().addOnline(new PlayerData(event.getUniqueId(), event.getName(), event.getAddress()), true);
			} catch (SQLException e) {
				e.printStackTrace();
				
				return;
			}
			
			return;
		}
		
		PlayerBanData data = plugin.getPlayerBanStorage().getBan(event.getUniqueId());
		
		if (data.hasExpired()) {
			try {
				plugin.getPlayerBanStorage().unban(data, plugin.getPlayerStorage().getConsole());
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return;
		}
		
		Message message = null;
		
		if (data.getExpires() == 0) {
			message = Message.get("disallowedPermBan");
		} else {
			message = Message.get("disallowedTempBan");
			message.set("expires", DateUtils.getDifferenceFormat(data.getExpires()));
		}
		
		message.set("player", data.getPlayer().getName());
		message.set("reason", data.getReason());
		message.set("actor", data.getActor().getName());
		
		event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, message.toString());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLogin(final PlayerLoginEvent event) {
		plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {

			public void run() {
				CloseableIterator<PlayerWarnData> warnings;
				try {
					warnings = plugin.getPlayerWarnStorage().getUnreadWarnings(plugin.getPlayerStorage().getOnline(event.getPlayer()));
					
					while (warnings.hasNext()) {
						PlayerWarnData warning = warnings.next();
						
						Message.get("warned")
							.set("displayName", event.getPlayer().getDisplayName())
							.set("player", event.getPlayer().getName())
							.set("reason", warning.getReason())
							.set("actor", warning.getActor().getName())
							.sendTo(event.getPlayer());
						
						warning.setRead(true);
						// TODO Move to one update query to set all warnings for player to read
						plugin.getPlayerWarnStorage().update(warning);
					}
					
					warnings.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}, 20L);

		if (!plugin.getDefaultConfig().isDuplicateIpCheckEnabled())
			return;
		
		plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new Runnable() {

			public void run() {
				final long ip = IPUtils.toLong(event.getAddress());
				final UUID uuid = event.getPlayer().getUniqueId();
				List<PlayerData> duplicates = plugin.getPlayerBanStorage().getDuplicates(ip);
				
				if (duplicates.size() == 0)
					return;
				
				StringBuilder sb = new StringBuilder();
				
				for (PlayerData player : duplicates) {
					if (player.getUUID().equals(uuid))
						continue;

					sb.append(player.getName());
					sb.append(", ");
				}
				
				sb.setLength(sb.length() - 2);
				
				Message message = Message.get("duplicateIP");
				message.set("player", event.getPlayer().getName());
				message.set("players", sb.toString());
				
				plugin.getServer().broadcast(message.toString(), "bm.notify.duplicateips");
			}
		}, 20L);
	}
}
