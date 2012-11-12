package me.confuserr.banmanager;

import me.confuserr.banmanager.BanManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnBanCommand implements CommandExecutor {

	private BanManager plugin;

	UnBanCommand(BanManager instance) {
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
			if(!player.hasPermission("bm.unban")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
		if(!offlinePlayer.isBanned()) {
			plugin.sendMessage(sender, plugin.banMessages.get("unbanError"));
		} else {
			offlinePlayer.setBanned(false);
			String offlineName = offlinePlayer.getName();
			plugin.dbLogger.banRemove(offlinePlayer.getName(), playerName);
			plugin.logger.info(plugin.banMessages.get("playerUnbanned").replace("[name]", offlineName));

			if(!sender.hasPermission("bm.notify"))
				plugin.sendMessage(sender, plugin.banMessages.get("playerUnbanned").replace("[name]", offlineName));
		}
		return true;
	}
	
}