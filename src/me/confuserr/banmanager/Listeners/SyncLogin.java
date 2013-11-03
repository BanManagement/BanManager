package me.confuserr.banmanager.Listeners;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;
import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.IPBanData;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class SyncLogin implements Listener {

	private BanManager plugin;

	public SyncLogin(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(final PlayerLoginEvent event) {
		final String name = event.getPlayer().getName().toLowerCase();
		final String ip = Util.getIP(event.getAddress());

		// Check to see if they are banned
		if (plugin.isPlayerBanned(name)) {
			BanData data = plugin.getPlayerBan(name);
			String banReason = "";

			long now = System.currentTimeMillis() / 1000;

			if (data.getExpires() == 0)
				banReason = plugin.getMessage("disconnectBan").replace("[reason]", data.getReason()).replace("[name]", name).replace("[by]", data.getBy());
			else {
				// Check to see if the temp ban has expired
				if (now >= data.getExpires()) {
					// It's expired, let them join!
					plugin.removePlayerBan(name, plugin.getMessage("consoleName"), true);
					event.allow();
					return;
				}

				banReason = plugin.getMessage("disconnectTempBan").replace("[name]", name).replace("[expires]", Util.formatDateDiff((long) data.getExpires() * 1000)).replace("[reason]", data.getReason()).replace("[by]", data.getBy());
			}

			banReason = Util.colorize(banReason);
			// Oh dear, they've been banned
			event.disallow(Result.KICK_BANNED, banReason);
			return;

		} else if (plugin.isIPBanned(ip)) {
			IPBanData data = plugin.getIPBan(ip);
			String ipReason = "";

			long now = System.currentTimeMillis() / 1000;

			if (data.getExpires() == 0)
				ipReason = plugin.getMessage("disconnectIpBan").replace("[reason]", data.getReason()).replace("[ip]", ip).replace("[by]", data.getBy());
			else {
				// Check to see if the temp ban has expired
				if (now >= data.getExpires()) {
					// It's expired, let them join!
					plugin.removeIPBan(ip, plugin.getMessage("consoleName"), true);
					event.allow();
					return;
				}

				ipReason = plugin.getMessage("disconnectTempIpBan").replace("[ip]", ip).replace("[expires]", Util.formatDateDiff((long) data.getExpires() * 1000)).replace("[reason]", data.getReason()).replace("[by]", data.getBy());
			}

			ipReason = Util.colorize(ipReason);
			// Oh dear, they've been banned
			event.disallow(Result.KICK_BANNED, ipReason);
			return;
		} else if (plugin.useBukkitBans()) {
			if (plugin.getServer().getOfflinePlayer(name).isBanned()) {
				if (plugin.dbLogger.handleBukkitBan(name))
					event.allow();
			}
		}

		// Here we check to see if player is muted, if they are, we add them to
		// the list!
		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

			public void run() {
				if (plugin.dbLogger.isMuted(name)) {
					plugin.getPlayerMutes().put(name, plugin.dbLogger.getMute(name));
				}
			}

		}, 2L);

		if (plugin.logIPs()) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					plugin.dbLogger.setIP(name, ip);
					
					// Check for duplicates!
					String players = plugin.dbLogger.findPlayerIpDuplicates(ip, name);

					if (!players.isEmpty()) {
						Util.sendMessageWithPerm(plugin.getMessage("duplicateIP").replace("[player]", name).replace("[players]", players), "bm.notify.duplicateips");
					}
				}
			}, 20L);
		}
	}
}
