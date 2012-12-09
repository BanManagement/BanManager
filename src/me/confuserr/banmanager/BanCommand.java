package me.confuserr.banmanager;

import java.util.List;

import me.confuserr.banmanager.BanManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanCommand implements CommandExecutor {

	private BanManager plugin;

	BanCommand(BanManager instance) {
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
			if(!player.hasPermission("bm.ban")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		String reason = plugin.getReason(args, 1);
		String viewReason = plugin.viewReason(reason);
		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		if(list.size() == 1) {
			Player target = list.get(0);
			if(target.getName().equals(playerName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("banSelfError"));
			} else if(target.hasPermission("bm.exempt.ban")) {
				plugin.sendMessage(sender, plugin.banMessages.get("banExemptError"));
			} else if(target.isBanned()) {
				plugin.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", target.getName()));
			} else {
				String kick = plugin.banMessages.get("banKick").replace("[name]", target.getDisplayName()).replace("[reason]", viewReason).replace("[by]", playerName);
				target.kickPlayer(kick);
				target.setBanned(true);
				plugin.dbLogger.logBan(target.getName(), playerName, reason);
				plugin.logger.info(plugin.banMessages.get("playerBanned").replace("[name]", target.getName()));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerBanned").replace("[name]", target.getName()));
				
				String message = plugin.banMessages.get("ban").replace("[name]", target.getDisplayName()).replace("[reason]", viewReason).replace("[by]", playerName);
				plugin.sendMessageWithPerm(message, "bm.notify");
			}
		}
		else if(list.size() > 1) {
			plugin.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
			return false;
		} else {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
			if(offlinePlayer.isBanned()) {
				plugin.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", offlinePlayer.getName()));
			} else {
				offlinePlayer.setBanned(true);
				plugin.dbLogger.logBan(offlinePlayer.getName(), playerName, reason);
				plugin.logger.info(plugin.banMessages.get("playerBanned").replace("[name]", offlinePlayer.getName()));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerBanned").replace("[name]", offlinePlayer.getName()));
				
				String message = plugin.banMessages.get("ban").replace("[name]", offlinePlayer.getName()).replace("[reason]", viewReason).replace("[by]", playerName);
				plugin.sendMessageWithPerm(message, "bm.notify");
			}
		}
		return true;
	}
}
