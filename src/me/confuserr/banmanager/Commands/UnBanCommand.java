package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;

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
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
		if(!plugin.bannedPlayers.contains(offlinePlayer.getName())) {
			plugin.sendMessage(sender, plugin.banMessages.get("unbanError"));
		} else {
			if(plugin.bukkitBan)
				offlinePlayer.setBanned(false);
			
			String offlineName = offlinePlayer.getName();
			plugin.dbLogger.banRemove(offlinePlayer.getName(), playerName);
			plugin.logger.info(plugin.banMessages.get("playerUnbanned").replace("[name]", offlineName));

			String message = plugin.banMessages.get("playerUnbanned").replace("[name]", offlineName);
			
			if(!sender.hasPermission("bm.notify"))
				plugin.sendMessage(sender, message);
			
			plugin.sendMessageWithPerm(message, "bm.notify");
		}
		return true;
	}
	
}