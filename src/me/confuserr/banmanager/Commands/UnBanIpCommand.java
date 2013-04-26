package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnBanIpCommand implements CommandExecutor {

	private BanManager plugin;

	public UnBanIpCommand(BanManager instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 1)
			return false;

		Player player = null;
		String consoleName = plugin.banMessages.get("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			consoleName = player.getName();
			if (!player.hasPermission("bm.unbanip")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		final String playerName = consoleName;

		if (Util.ValidateIPAddress(args[0])) {
			// Its an IP
			String ip = args[0];
			if (plugin.bannedIps.contains(ip)) {
				if (plugin.bukkitBan)
					plugin.getServer().unbanIP(ip);

				plugin.dbLogger.ipRemove(ip, playerName);
				Util.sendMessage(sender, plugin.banMessages.get("ipUnbanned").replace("[ip]", ip).replace("[by]", playerName));
			} else {
				Util.sendMessage(sender, plugin.banMessages.get("ipNotBannedError").replace("[ip]", ip));
			}

		} else {
			// Assume its a player!
			if(!StringUtils.isAlphanumeric(args[0])) {
				Util.sendMessage(sender, plugin.banMessages.get("invalidPlayer"));
				return true;
			}
			
			final String offlineName = plugin.getServer().getOfflinePlayer(args[0]).getName();
			final String byName = playerName;
			
			plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

				public void run() {
					String ip = plugin.dbLogger.getIP(offlineName);

					if (ip.isEmpty())
						Util.sendMessage(sender, plugin.banMessages.get("ipPlayerOfflineError").replace("[name]", offlineName));
					else {
						// Ok, we have their IP, lets ban it
						if(plugin.bukkitBan)
							plugin.getServer().unbanIP(ip);
						
						plugin.dbLogger.ipRemove(ip, byName);
						Util.sendMessage(sender, plugin.banMessages.get("ipUnbanned").replace("[ip]", ip).replace("[by]", playerName));
					}
				}
			});
		}

		return true;
	}

}
