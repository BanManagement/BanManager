package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnBanIpAllCommand implements CommandExecutor {

	private BanManager plugin;

	public UnBanIpAllCommand(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 1)
			return false;

		Player player = null;
		String consoleName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			consoleName = player.getName();
			if (!player.hasPermission("bm.unbanipall")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		final String playerName = consoleName;

		if (Util.ValidateIPAddress(args[0])) {
			// Its an IP
			String ip = args[0];
			if (plugin.isIPBanned(ip)) {
				if (sender.hasPermission("bm.ipunban.by")) {
					if (!plugin.getIPBan(ip).getBy().equals(playerName) && !sender.hasPermission("bm.exempt.override.ipban")) {
						Util.sendMessage(sender, plugin.getMessage("commandPermissionError"));
						return true;
					}
				}
				
				plugin.removeExternalIPBan(ip, playerName);
				Util.sendMessage(sender, plugin.getMessage("ipUnbanned").replace("[ip]", ip).replace("[by]", playerName));
			} else {
				Util.sendMessage(sender, plugin.getMessage("ipNotBannedError").replace("[ip]", ip));
			}

		} else {
			// Assume its a player!
			if (!Util.isValidPlayerName(args[0])) {
				Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
				return true;
			}

			final String offlineName = plugin.getServer().getOfflinePlayer(args[0]).getName();
			final String byName = playerName;

			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					String ip = plugin.dbLogger.getIP(offlineName);

					if (ip.isEmpty())
						Util.sendMessage(sender, plugin.getMessage("ipPlayerOfflineError").replace("[name]", offlineName));
					else {
						if (sender.hasPermission("bm.ipunban.by")) {
							if (!plugin.getIPBan(ip).getBy().equals(playerName) && !sender.hasPermission("bm.exempt.override.ipban")) {
								Util.sendMessage(sender, plugin.getMessage("commandPermissionError"));
								return;
							}
						}

						// Ok, we have their IP, lets unban it
						plugin.removeExternalIPBan(ip, byName);

						String message = plugin.getMessage("ipUnbanned").replace("[ip]", ip).replace("[by]", playerName);

						if (!sender.hasPermission("bm.notify.unipban"))
							Util.sendMessage(sender, message);

						Util.sendMessageWithPerm(message, "bm.notify.unipban");
					}
				}
			});
		}

		return true;
	}

}
