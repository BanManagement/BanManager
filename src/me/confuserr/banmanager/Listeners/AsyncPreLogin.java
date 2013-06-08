package me.confuserr.banmanager.Listeners;

import java.net.InetAddress;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

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
		final String name = event.getName();
		final InetAddress ip = event.getAddress();
		final String ipStr = plugin.getIp(ip);

		if (plugin.bukkitBan) {
			// Check to see if they are to be unbanned
			if (plugin.toUnbanPlayer.contains(name)) {
				// Ok they are, no need to do additional checks!
				// But we unban them now otherwise they'll get the Ban Hammer
				// message
				// This is experimental, as its an async event, shouldn't really
				// be calling a main thread method, shall see how it goes for
				// now!
				plugin.getServer().getOfflinePlayer(name).setBanned(false);
				return;
			}
		}

		if (plugin.bukkitBan) {
			// Do the same as above but for IP
			if (plugin.toUnbanIp.contains(ipStr)) {
				// But we unban them now otherwise they'll get the Ban Hammer
				// message
				plugin.getServer().unbanIP(ipStr);
				return;
			}
		}

		// Here we check to see if player is muted, if they are, we add them to
		// the list!
		if (!plugin.mutedPlayersBy.containsKey(name)) {
			plugin.dbLogger.isMutedThenAdd(name);
		}

		// Here we log their IP to the database
		if (plugin.logIPs)
			plugin.dbLogger.setIP(name, ip);

		// Check to see if they are banned
		if (plugin.bannedPlayers.contains(name.toLowerCase()) || plugin.bannedIps.contains(ipStr)) {

			String banReason = plugin.dbLogger.isBanned(name);
			if (!banReason.isEmpty()) {
				banReason = Util.colorize(banReason);
				// Oh dear, they've been banned
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, banReason);
				return;
			} else {
				String ipReason = plugin.dbLogger.isBanned(ip);
				if (!ipReason.isEmpty()) {
					ipReason = Util.colorize(ipReason);
					// Oh dear, they've been banned
					event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_BANNED, ipReason);
					return;
				}
			}
		}

		if (plugin.logIPs) {
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					// Check for duplicates!
					String players = plugin.dbLogger.findPlayerIpDuplicates(ipStr, name);

					if (!players.isEmpty()) {
						Util.sendMessageWithPerm(plugin.banMessages.get("duplicateIP").replace("[player]", name).replace("[players]", players), "bm.notify.duplicateips");
					}
				}
			}, 20L);
		}
	}
}
