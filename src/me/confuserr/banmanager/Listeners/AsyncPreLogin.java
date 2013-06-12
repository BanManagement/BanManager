package me.confuserr.banmanager.Listeners;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;
import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.IPBanData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class AsyncPreLogin implements Listener {

	private BanManager plugin;

	public AsyncPreLogin(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(final AsyncPlayerPreLoginEvent event) {
		final String name = event.getName().toLowerCase();
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
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banReason);
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
			event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ipReason);
		} else if (plugin.useBukkitBans()) {
			if (plugin.getServer().getOfflinePlayer(name).isBanned()) {
				if (plugin.dbLogger.handleBukkitBan(name))
					event.allow();
			}
		}

		// Here we check to see if player is muted, if they are, we add them to
		// the list!
		if (plugin.dbLogger.isMuted(name))
			plugin.getPlayerMutes().put(name, plugin.dbLogger.getMute(name));

		if (plugin.logIPs()) {// Here we log their IP to the database
			plugin.dbLogger.setIP(name, ip);

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
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
