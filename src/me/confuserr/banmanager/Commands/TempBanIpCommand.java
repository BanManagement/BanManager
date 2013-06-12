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

public class TempBanIpCommand implements CommandExecutor {

	private BanManager plugin;

	public TempBanIpCommand(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 3)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		long timeExpires = Util.getTimeStamp(args[1]);

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.tempbanip")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			} else {
				if (!player.hasPermission("bm.timelimit.bans.bypass")) {
					for (String k : plugin.getTimeLimitsBans().keySet()) {
						if (player.hasPermission("bm.timelimit.bans." + k)) {
							long timeLimit = Util.getTimeStamp(plugin.getTimeLimitsBans().get(k));
							if (timeLimit < timeExpires) {
								// Erm, they tried to ban for too long
								Util.sendMessage(player, plugin.getMessage("banTimeLimitError"));
								return true;
							}
						}
					}
				}
			}
		}

		if (timeExpires == 0) {
			Util.sendMessage(sender, plugin.getMessage("illegalDateError"));
			return true;
		}

		final String reason = Util.getReason(args, 2);
		final String viewReason = Util.viewReason(reason);

		final long timeExpires2 = timeExpires / 1000;
		final String formatExpires = Util.formatDateDiff(timeExpires);
		
		if (Util.ValidateIPAddress(args[0])) {
			// Its an IP
			String ip = args[0];

			ban(sender, ip, playerName, reason, viewReason, timeExpires2, formatExpires);

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
								ban(sender, ip, byName, reason, viewReason, timeExpires2, formatExpires);
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

							ban(sender, ip, byName, reason, viewReason, timeExpires2, formatExpires);
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

							ban(sender, ip, byName, reason, viewReason, timeExpires2, formatExpires);
						}
					});
				}
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
				return true;
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
							ban(sender, ip, byName, reason, viewReason, timeExpires2, formatExpires);
						}
					}
				});
			}

		}
		return true;
	}

	private void ban(CommandSender sender, final String ip, String bannedByName, String reason, String viewReason, Long timeExpires, String formatExpires) {
		
		if (plugin.getIPBans().get(ip) != null) {
			Util.sendMessage(sender, plugin.getMessage("alreadyBannedError").replace("[name]", ip));
			return;
		}
		
		final String kick = plugin.getMessage("ipTempBanKick").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", bannedByName).replace("[expires]", formatExpires);

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

		plugin.dbLogger.logTempIpBan(ip, bannedByName, reason, timeExpires);
		plugin.getLogger().info(plugin.getMessage("ipBanned").replace("[ip]", ip));

		if (!sender.hasPermission("bm.notify"))
			Util.sendMessage(sender, plugin.getMessage("ipTempBanned").replace("[ip]", ip).replace("[expires]", formatExpires));

		String message = plugin.getMessage("ipTempBan").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", bannedByName);
		Util.sendMessageWithPerm(message, "bm.notify");
	}
}