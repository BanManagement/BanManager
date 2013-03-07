package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class KickCommand implements CommandExecutor {

	private BanManager plugin;

	public KickCommand(BanManager instance) {
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
			if(!player.hasPermission("bm.kick")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		if(list.size() == 1) {
			Player target = list.get(0);
			if(target.getName().equals(playerName)) {
				Util.sendMessage(sender, plugin.banMessages.get("kickSelfError"));
			} else if(target.hasPermission("bm.exempt.kick")) {
				Util.sendMessage(sender, plugin.banMessages.get("kickExemptError"));
			} else {
				
				String reason = "";
				String kick = "";
				String message = "";
				
				if(args.length > 1)
					reason = Util.getReason(args, 1);
				
				String viewReason = Util.viewReason(reason);
				
				if(reason.isEmpty())
					kick = plugin.banMessages.get("kickNoReason").replace("[name]", target.getDisplayName()).replace("[by]", playerName);
				else
					kick = plugin.banMessages.get("kickReason").replace("[name]", target.getDisplayName()).replace("[reason]", viewReason).replace("[by]", playerName);
				
				target.kickPlayer(kick);
				
				if(plugin.logKicks)
					plugin.dbLogger.logKick(target.getName(), playerName, reason);
				
				plugin.logger.info(plugin.banMessages.get("playerKicked").replace("[name]", target.getName()));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.logger.info(plugin.banMessages.get("kickReason").replace("[name]", target.getName()));
				
				if(reason.isEmpty())
					message = plugin.banMessages.get("kickedNo").replace("[displayName]", target.getDisplayName()).replace("[name]", target.getName()).replace("[by]", playerName);
				else
					message = plugin.banMessages.get("kicked").replace("[displayName]", target.getDisplayName()).replace("[name]", target.getName()).replace("[reason]", viewReason).replace("[by]", playerName);
				
				Util.sendMessageWithPerm(message, "bm.notify");
			}
		}
		else if(list.size() > 1) {
			Util.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
			return false;
		} else {
			Util.sendMessage(sender, plugin.banMessages.get("playerNotOnline"));
			return false;
		}
		
		return true;
	}

}
