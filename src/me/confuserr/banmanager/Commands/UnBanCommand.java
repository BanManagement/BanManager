package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnBanCommand implements CommandExecutor {

	private BanManager plugin;

	public UnBanCommand(BanManager instance) {
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
			if(!player.hasPermission("bm.unban")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
		if(!plugin.bannedPlayers.contains(offlinePlayer.getName().toLowerCase())) {
			Util.sendMessage(sender, plugin.banMessages.get("unbanError"));
		} else {
			if(plugin.bukkitBan)
				offlinePlayer.setBanned(false);
			
			String offlineName = offlinePlayer.getName();
			plugin.dbLogger.banRemove(offlinePlayer.getName(), playerName);
			
			String message = plugin.banMessages.get("playerUnbanned").replace("[name]", offlineName).replace("[by]", playerName);
			
			plugin.logger.info(message);
			
			if(!sender.hasPermission("bm.notify"))
				Util.sendMessage(sender, message);
			
			Util.sendMessageWithPerm(message, "bm.notify");
		}
		return true;
	}
	
}