package me.confuserr.banmanager;

import me.confuserr.banmanager.BanManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnMuteCommand implements CommandExecutor {

	private BanManager plugin;

	UnMuteCommand(BanManager instance) {
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
			if(!player.hasPermission("bm.unmute")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
		String offlineName = offlinePlayer.getName();

		if(!plugin.dbLogger.isMuted(offlineName)) {
			plugin.sendMessage(sender, plugin.banMessages.get("playerNotMutedError"));
		} else {			
			plugin.removeMute(offlineName, playerName);
		
			String message = plugin.banMessages.get("playerUnmuted").replace("[name]", offlineName);
		
			plugin.logger.info(message);
			
			if(!sender.hasPermission("bm.notify"))
				plugin.sendMessage(sender, message);
			
			plugin.sendMessageWithPerm(message, "bm.notify");
		}
		return true;
	}
	
}