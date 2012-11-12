package me.confuserr.banmanager;

import java.util.List;
import me.confuserr.banmanager.BanManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TempBanCommand implements CommandExecutor {

	private BanManager plugin;

	TempBanCommand(BanManager instance) {
		plugin = instance;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {		
		if(args.length < 3)
			return false;
		
		Player player = null;
		String playerName = "Console";
		
		if(sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if(!player.hasPermission("bm.tempban")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		String reason = plugin.getReason(args, 2);
		String viewReason = plugin.viewReason(reason);
		Long timeExpires = getTimeStamp(args[1]);
		
		if(timeExpires == 0) {
			plugin.sendMessage(sender, plugin.banMessages.get("illegalDateError"));
			return true;
		}
		timeExpires = timeExpires / 1000;
		String formatExpires = plugin.formatDateDiff(timeExpires * 1000);
		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		
		if(list.size() == 1) {
			Player target = list.get(0);
			if(target.getName().equals(playerName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("banSelfError"));
			} else if(target.hasPermission("bm.exempt")) {
				plugin.sendMessage(sender, plugin.banMessages.get("banExemptError"));
			} else if(target.isBanned()) {
				plugin.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", target.getName()));
			} else {
				String kick = plugin.banMessages.get("tempBanKick").replace("[name]", target.getDisplayName()).replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", playerName);
				target.kickPlayer(kick);
				target.setBanned(true);
				
				plugin.dbLogger.logTempBan(target.getName(), playerName, reason, timeExpires);
				plugin.logger.info(plugin.banMessages.get("playerTempBanned").replace("[name]", target.getName()));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerTempBanned").replace("[name]", target.getName()));
				
				String message = plugin.banMessages.get("tempBan").replace("[name]", target.getDisplayName()).replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", playerName);
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
				plugin.dbLogger.logTempBan(offlinePlayer.getName(), playerName, reason, timeExpires);
				plugin.logger.info(plugin.banMessages.get("playerTempBanned").replace("[name]", offlinePlayer.getName()));
				
				if(!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerTempBanned").replace("[name]", offlinePlayer.getName()));
				
				String message = plugin.banMessages.get("tempBan").replace("[name]", offlinePlayer.getName()).replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", playerName);
				plugin.sendMessageWithPerm(message, "bm.notify");
			}
		}
		return true;
	}
	
	private long getTimeStamp(String time) {
		// TODO Auto-generated method stub
		long timeReturn;
		try {
			timeReturn = plugin.parseDateDiff(time, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			timeReturn = 0;
		}
		return timeReturn;
	}
}