package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanInfoCommand implements CommandExecutor {

	private BanManager plugin;

	public BanInfoCommand(BanManager instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {		
		
		if(args.length != 1)
			return false;
		
		Player player = null;
		
		if(sender instanceof Player) {
			player = (Player) sender;
			if(!player.hasPermission("bm.baninfo")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		if(list.size() == 1) {
			String target = list.get(0).getName();
			plugin.sendMessage(sender, plugin.banMessages.get("banInfo").replace("[name]", target).replace("[currentBan]", plugin.dbLogger.getCurrentBanInfo(target)).replace("[previousBans]", Integer.toString(plugin.dbLogger.getPastBanCount(target))));
		}
		else if(list.size() > 1) {
			plugin.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
			return false;
		} else {
			// Possible offline player
			String target = args[0];
			plugin.sendMessage(sender, plugin.banMessages.get("banInfo").replace("[name]", target).replace("[currentBan]", plugin.dbLogger.getCurrentBanInfo(target)).replace("[previousBans]", Integer.toString(plugin.dbLogger.getPastBanCount(target))));
		}		
		return true;
	}
}