package me.confuserr.banmanager;

import java.util.List;

import me.confuserr.banmanager.BanManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteCommand implements CommandExecutor {

	private BanManager plugin;

	MuteCommand(BanManager instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if(args.length < 2)
			return false;
		
		Player player = null;
		String playerName = "Console";
		
		if(sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if(!player.hasPermission("bm.mute")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		String reason = plugin.getReason(args, 1);
		String viewReason = plugin.viewReason(reason);
		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		if(list.size() == 1) {
			Player target = list.get(0);
			String targetName = target.getName();
			if(targetName.equals(playerName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("muteSelfError"));
			} else if(target.hasPermission("bm.exempt.mute")) {
				plugin.sendMessage(sender, plugin.banMessages.get("muteExemptError"));
			} else if(plugin.mutedPlayersBy.containsKey(targetName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("alreadyMutedError").replace("[name]", targetName));
			} else {
				plugin.addMute(targetName, reason, playerName, (long) 0);
				plugin.dbLogger.logMute(targetName, playerName, reason);
				plugin.logger.info(plugin.banMessages.get("playerMuted").replace("[name]", targetName));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerMuted").replace("[name]", targetName));
				
				String message = plugin.banMessages.get("mute").replace("[name]", target.getDisplayName()).replace("[reason]", viewReason).replace("[by]", playerName);
				plugin.sendMessageWithPerm(message, "bm.notify");
				
				// Inform the player they have been muted
				String mutedMessage = plugin.banMessages.get("muted").replace("[reason]", viewReason).replace("[by]", playerName);
				target.sendMessage(mutedMessage);
			}
		}
		else if(list.size() > 1) {
			plugin.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
			return false;
		} else {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
			String offlineName = offlinePlayer.getName();
			if(plugin.dbLogger.isMuted(offlineName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("alreadyMutedError").replace("[name]", offlineName));
			} else {
				plugin.addMute(offlineName, reason, playerName, (long) 0);
				plugin.dbLogger.logMute(offlineName, playerName, reason);
				plugin.logger.info(plugin.banMessages.get("playerMuted").replace("[name]", offlineName));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerMuted").replace("[name]", offlineName));
				
				String message = plugin.banMessages.get("mute").replace("[name]", offlineName).replace("[reason]", viewReason).replace("[by]", playerName);
				plugin.sendMessageWithPerm(message, "bm.notify");
			}
		}
		return true;
	}
}
