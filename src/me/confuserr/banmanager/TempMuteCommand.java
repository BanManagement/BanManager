package me.confuserr.banmanager;

import java.util.List;
import me.confuserr.banmanager.BanManager;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempMuteCommand implements CommandExecutor {

	private BanManager plugin;

	TempMuteCommand(BanManager instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 3)
			return false;

		Player player = null;
		String playerName = "Console";

		Long timeExpires = getTimeStamp(args[1]);

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.tempmute")) {
				plugin.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			} else {
				if (!player.hasPermission("bm.timelimit.mutes.bypass")) {
					for (String k : plugin.timeLimitsMutes.keySet()) {
						if (player.hasPermission("bm.timelimit.mutes." + k)) {
							long timeLimit = getTimeStamp(plugin.timeLimitsMutes.get(k));
							if (timeLimit < timeExpires) {
								// Erm, they tried to ban for too long
								plugin.sendMessage(player, plugin.banMessages.get("muteTimeLimitError"));
								return true;
							}
						}
					}
				}
			}
		}

		String reason = plugin.getReason(args, 2);
		String viewReason = plugin.viewReason(reason);

		if (timeExpires == 0) {
			plugin.sendMessage(sender, plugin.banMessages.get("illegalDateError"));
			return true;
		}
		timeExpires = timeExpires / 1000;
		String formatExpires = plugin.formatDateDiff(timeExpires * 1000);
		List<Player> list = plugin.getServer().matchPlayer(args[0]);

		if (list.size() == 1) {
			Player target = list.get(0);
			String targetName = target.getName();
			if (targetName.equals(playerName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("muteSelfError"));
			} else if (target.hasPermission("bm.exempt")) {
				plugin.sendMessage(sender, plugin.banMessages.get("muteExemptError"));
			} else if (plugin.mutedPlayersBy.containsKey(targetName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("alreadyMutedError").replace("[name]", targetName));
			} else {
				plugin.addMute(targetName, reason, playerName, timeExpires);
				plugin.dbLogger.logTempMute(targetName, playerName, reason, timeExpires);
				plugin.logger.info(plugin.banMessages.get("playerMuted").replace("[name]", targetName));

				if (!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerMuted").replace("[name]", targetName));

				String message = plugin.banMessages.get("tempMute").replace("[name]", target.getDisplayName()).replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", playerName);
				plugin.sendMessageWithPerm(message, "bm.notify");

				// Inform the player they have been muted
				String mutedMessage = plugin.banMessages.get("tempMuted").replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", playerName);
				target.sendMessage(mutedMessage);
			}
		} else if (list.size() > 1) {
			plugin.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
			return false;
		} else {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
			String offlineName = offlinePlayer.getName();
			if (plugin.dbLogger.isMuted(offlineName)) {
				plugin.sendMessage(sender, plugin.banMessages.get("alreadyMutedError").replace("[name]", offlineName));
			} else {

				plugin.addMute(offlineName, reason, playerName, timeExpires);
				plugin.dbLogger.logMute(offlineName, playerName, reason);
				plugin.logger.info(plugin.banMessages.get("playerTempMuted").replace("[name]", offlineName));

				if (!sender.hasPermission("bm.notify"))
					plugin.sendMessage(sender, plugin.banMessages.get("playerTempMuted").replace("[name]", offlineName));

				String message = plugin.banMessages.get("tempMute").replace("[name]", offlineName).replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", playerName);
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