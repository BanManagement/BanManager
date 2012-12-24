package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;

import org.bukkit.OfflinePlayer;
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
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {		
		if(args.length < 1)
			return false;
		
		Player player = null;
		String playerName = plugin.banMessages.get("consoleName");
		
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
			// Assume its a player!
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
			
			String ip = plugin.dbLogger.getIP(offlinePlayer.getName());

			if(ip.isEmpty())
				plugin.sendMessage(sender, plugin.banMessages.get("ipPlayerOfflineError").replace("[name]", offlinePlayer.getName()));
			else {
				// Ok, we have their IP, lets ban it
				plugin.getServer().unbanIP(ip);
				plugin.dbLogger.ipRemove(ip, playerName);
				plugin.sendMessage(sender, plugin.banMessages.get("ipUnbanned").replace("[ip]", ip));
			}
			//plugin.sendMessage(sender, plugin.banMessages.get("invalidIp"));
		}

		return true;
	}

}
