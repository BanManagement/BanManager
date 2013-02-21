package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LoglessKickCommand implements CommandExecutor {

	private BanManager plugin;

	public LoglessKickCommand(BanManager instance) {
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
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		if(list.size() == 1) {
			Player target = list.get(0);
			if(target.getName().equals(playerName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("kickSelfError"));
			} else if(target.hasPermission("bm.exempt")) {
				plugin.sendMessage(sender, plugin.banMessages.get("kickExemptError"));
			} else {
				
				String reason = "";
				String kick = "";
				String message = "";
				
				if(args.length > 1)
					reason = plugin.getReason(args, 1);
				
				String viewReason = plugin.viewReason(reason);
				
				if(reason.isEmpty())
					kick = plugin.banMessages.get("kickNoReason").replace("[name]", target.getDisplayName()).replace("[by]", playerName);
				else
					kick = plugin.banMessages.get("kickReason").replace("[name]", target.getDisplayName()).replace("[reason]", viewReason).replace("[by]", playerName);
				
				target.kickPlayer(kick);
				
				plugin.logger.info(plugin.banMessages.get("playerKicked").replace("[name]", target.getName()));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.logger.info(plugin.banMessages.get("kickReason").replace("[name]", target.getName()));
				
				if(reason.isEmpty())
					message = plugin.banMessages.get("kickedNo").replace("[name]", target.getDisplayName()).replace("[by]", playerName);
				else
					message = plugin.banMessages.get("kicked").replace("[name]", target.getDisplayName()).replace("[reason]", viewReason).replace("[by]", playerName);
				
				plugin.sendMessageWithPerm(message, "bm.notify");
			}
		}
		else if(list.size() > 1) {
			plugin.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
			return false;
		} else {
			plugin.sendMessage(sender, plugin.banMessages.get("playerNotOnline"));
			return false;
		}
		
		return true;
	}

}
