package me.confuserr.banmanager.Commands;

import java.net.InetAddress;
import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanIpAllCommand implements CommandExecutor {

	private BanManager plugin;

	public BanIpAllCommand(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 2)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.banipall")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		final String reason = Util.getReason(args, 1);
		final String viewReason = Util.viewReason(reason);

		if (Util.ValidateIPAddress(args[0])) {
			// Its an IP
			String ip = args[0];

			ban(sender, ip, playerName, reason, viewReason);

		} else {

			if(!Util.isValidPlayerName(args[0])) {
				Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
				return true;
			}
			
			final String byName = playerName;

			// Its a player!
			if (!plugin.usePartialNames()) {
				if (plugin.getServer().getPlayerExact(args[0]) == null) {
					// Offline player
					OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);

					final String pName = offlinePlayer.getName();

					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

						public void run() {
							String ip = plugin.dbLogger.getIP(pName);

							if (ip.isEmpty())
								Util.sendMessage(sender, plugin.getMessage("ipPlayerOfflineError").replace("[name]", pName));
							else {
								// Ok, we have their IP, lets ban it
								ban(sender, ip, byName, reason, viewReason);
							}
						}
					});
				} else {
					// Online
					Player target = plugin.getServer().getPlayerExact(args[0]);

					final InetAddress targetIp = target.getAddress().getAddress();

					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

						public void run() {
							String ip = Util.getIP(targetIp);

							ban(sender, ip, byName, reason, viewReason);
						}
					});
				}
				return true;
			}

			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				if (target.getName().equals(playerName)) {
					Util.sendMessage(sender, plugin.getMessage("ipSelfError"));
				} else if (!sender.hasPermission("bm.exempt.override.banip") && target.hasPermission("bm.exempt.banip")) {
					Util.sendMessage(sender, plugin.getMessage("banExemptError"));
				} else {

					final InetAddress targetIp = target.getAddress().getAddress();

					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

						public void run() {
							String ip = Util.getIP(targetIp);

							ban(sender, ip, byName, reason, viewReason);
						}
					});
				}
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
				return false;
			} else {
				// They're offline, lets check the database
				OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);

				final String pName = offlinePlayer.getName();

				plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

					public void run() {
						String ip = plugin.dbLogger.getIP(pName);

						if (ip.isEmpty())
							Util.sendMessage(sender, plugin.getMessage("ipPlayerOfflineError").replace("[name]", pName));
						else {
							// Ok, we have their IP, lets ban it
							ban(sender, ip, byName, reason, viewReason);
						}
					}
				});
			}

		}

		return true;
	}

	private void ban(CommandSender sender, final String ip, String bannedByName, String reason, String viewReason) {

		if (plugin.isIPBanned(ip)) {
			Util.sendMessage(sender, plugin.getMessage("alreadyBannedError").replace("[name]", ip));
			return;
		}
		
		final String kick = plugin.getMessage("ipBanKick").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", bannedByName);

		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
				for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
					if (Util.getIP(onlinePlayer.getAddress().toString()).equals(ip)) {

						onlinePlayer.kickPlayer(kick);
					}
				}
			}
		});

		if (plugin.useBukkitBans())
			plugin.getServer().banIP(ip);

		plugin.addExternalIPBan(ip, bannedByName, reason);
		plugin.getServer().getConsoleSender().sendMessage(plugin.getMessage("ipBanned").replace("[ip]", ip));

		if (!sender.hasPermission("bm.notify.ipban"))
			Util.sendMessage(sender, plugin.getMessage("ipBanned").replace("[ip]", ip));

		String message = plugin.getMessage("ipBan").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", bannedByName);
		Util.sendMessageWithPerm(message, "bm.notify.ipban");
	}
}
