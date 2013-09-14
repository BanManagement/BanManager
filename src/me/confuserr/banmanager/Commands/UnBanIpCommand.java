package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnBanIpCommand implements CommandExecutor {

	private BanManager plugin;

	public UnBanIpCommand(BanManager instance) {
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
			if (!player.hasPermission("bm.unbanip")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		final String playerName = consoleName;

		if (Util.ValidateIPAddress(args[0])) {
			// Its an IP
			String ip = args[0];
			if (plugin.isIPBanned(ip)) {
				plugin.removeIPBan(ip, playerName, true);
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
						// Ok, we have their IP, lets unban it
						plugin.removeIPBan(ip, byName, true);

						String message = plugin.getMessage("ipUnbanned").replace("[ip]", ip).replace("[by]", playerName);
						
						plugin.getLogger().info(message);

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
