package me.confuserr.banmanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnBanIpCommand implements CommandExecutor {

	private BanManager plugin;

	UnBanIpCommand(BanManager instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {		
		if(args.length < 1)
			return false;
		
		Player player = null;
		String playerName = "Console";
		
		if(sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if(!player.hasPermission("bm.unbanip")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		if(BanManager.ValidateIPAddress(args[0])) {
			// Its an IP
			String ip = args[0];
			if(plugin.dbLogger.ipBanned(ip)) {
				plugin.getServer().unbanIP(ip);
				plugin.dbLogger.ipRemove(ip, playerName);
				plugin.sendMessage(sender, plugin.banMessages.get("ipUnbanned").replace("[ip]", ip));
			} else {
				plugin.sendMessage(sender, plugin.banMessages.get("ipNotBannedError").replace("[ip]", ip));
			}
			
		} else {
			plugin.sendMessage(sender, plugin.banMessages.get("invalidIp"));
		}

		return true;
	}

}
