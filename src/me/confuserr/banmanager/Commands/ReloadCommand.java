package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {

	private BanManager plugin;

	public ReloadCommand(BanManager instance) {
		plugin = instance;
	}
	
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		
		if(sender instanceof Player) {
			Player player = (Player) sender;

			if(!player.hasPermission("bm.reload")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		plugin.configReload();
		
		return true;
	}
}
