package me.confuserr.banmanager;

import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanIpCommand implements CommandExecutor {
	
	private BanManager plugin;
	
	BanIpCommand (BanManager instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if(args.length < 2)
			return false;
		
		Player player = null;
		String playerName = plugin.banMessages.get("consoleName");
		
		if(sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if(!player.hasPermission("bm.banip")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		String reason = plugin.getReason(args, 1);
		String viewReason = plugin.viewReason(reason);
		
		if(BanManager.ValidateIPAddress(args[0])) {
			// Its an IP
			String ip = args[0];
			plugin.getServer().banIP(ip);
			plugin.dbLogger.logIpBan(ip, playerName, reason);
			plugin.sendMessage(sender, plugin.banMessages.get("ipBanned").replace("[ip]", ip));
			
			String kick = plugin.banMessages.get("ipBanKick").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", playerName);
			
			for(Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
				if(plugin.getIp(onlinePlayer.getAddress().toString()).equals(ip))
					onlinePlayer.kickPlayer(kick);
			}
			
			String message = plugin.banMessages.get("ipBan").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", playerName);
			plugin.sendMessageWithPerm(message, "bm.notify");
			
		} else {
			// Its a player!
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if(list.size() == 1) {
				Player target = list.get(0);
				if(target.getName().equals(playerName)) {
					plugin.sendMessage(sender, plugin.banMessages.get("ipSelfError"));
				} else if(target.hasPermission("bm.exempt.banip")) {
					plugin.sendMessage(sender, plugin.banMessages.get("banExemptError"));											
				} else {
					String ip = plugin.getIp(target.getAddress().getAddress().toString());
					String kick = "";
					// Find users with same ip and kick them
					for(Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
						if(plugin.getIp(onlinePlayer.getAddress().toString()).equals(ip)) {
							kick = plugin.banMessages.get("ipBanKick").replace("[ip]", ip).replace("[reason]", viewReason).replace("[name]", onlinePlayer.getName()).replace("[by]", playerName);
							onlinePlayer.kickPlayer(kick);
						}
					}
					
					plugin.getServer().banIP(ip);
					plugin.dbLogger.logIpBan(ip, playerName, reason);
					plugin.logger.info(plugin.banMessages.get("ipBanned").replace("[ip]", ip));
				
					if(!sender.hasPermission("bm.notify"))
						plugin.sendMessage(sender, plugin.banMessages.get("ipBanned").replace("[ip]", ip));
					
					String message = plugin.banMessages.get("ipBan").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", playerName);
					plugin.sendMessageWithPerm(message, "bm.notify");
				}
			}
			else if(list.size() > 1) {
				plugin.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
				return false;
			} else {
				// They're offline, lets check the database
				OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
				
				String ip = plugin.dbLogger.getIP(offlinePlayer.getName());

				if(ip.isEmpty())
					plugin.sendMessage(sender, plugin.banMessages.get("ipPlayerOfflineError").replace("[name]", offlinePlayer.getName()));
				else {
					// Ok, we have their IP, lets ban it
					plugin.getServer().banIP(ip);
					plugin.dbLogger.logIpBan(ip, playerName, reason);
					plugin.logger.info(plugin.banMessages.get("ipBanned").replace("[ip]", ip));
					
					if(!sender.hasPermission("bm.notify"))
						plugin.sendMessage(sender, plugin.banMessages.get("ipBanned").replace("[ip]", ip));
					
					String message = plugin.banMessages.get("ipBan").replace("[ip]", ip).replace("[reason]", viewReason).replace("[by]", playerName);
					plugin.sendMessageWithPerm(message, "bm.notify");
				}
			}
			
		}
	
		return true;
	}
}
