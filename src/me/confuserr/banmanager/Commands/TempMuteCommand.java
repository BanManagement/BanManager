package me.confuserr.banmanager.Commands;

import java.util.List;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempMuteCommand implements CommandExecutor {

	private BanManager plugin;

	public TempMuteCommand(BanManager instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 3)
			return false;

		Player player = null;
		String playerName = plugin.banMessages.get("consoleName");

		Long timeExpires = getTimeStamp(args[1]);

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.tempmute")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			} else {
				if (!player.hasPermission("bm.timelimit.mutes.bypass")) {
					for (String k : plugin.timeLimitsMutes.keySet()) {
						if (player.hasPermission("bm.timelimit.mutes." + k)) {
							long timeLimit = getTimeStamp(plugin.timeLimitsMutes.get(k));
							if (timeLimit < timeExpires) {
								// Erm, they tried to ban for too long
								Util.sendMessage(player, plugin.banMessages.get("muteTimeLimitError"));
								return true;
							}
						}
					}
				}
			}
		}
		
		if(!StringUtils.isAlphanumeric(args[0])) {
			Util.sendMessage(sender, plugin.banMessages.get("invalidPlayer"));
			return true;
		}

		String reason = Util.getReason(args, 2);
		String viewReason = Util.viewReason(reason);

		if (timeExpires == 0) {
			Util.sendMessage(sender, plugin.banMessages.get("illegalDateError"));
			return true;
		}
		timeExpires = timeExpires / 1000;
		String formatExpires = plugin.formatDateDiff(timeExpires * 1000);

		if (plugin.usePartialNames) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				mute(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason, timeExpires, formatExpires);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				mute(sender, args[0], args[0], playerName, false, reason, viewReason, timeExpires, formatExpires);
			}
		} else {
			// Must be exact name
			if(plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				mute(sender, args[0], args[0], playerName, false, reason, viewReason, timeExpires, formatExpires);
			} else {
				// Online
				Player target = plugin.getServer().getPlayerExact(args[0]);
				mute(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason, timeExpires, formatExpires);
			}
		}
		return true;
	}

	private void mute(CommandSender sender, String playerName, String playerDisplayName, String mutedByName, boolean online, String reason, String viewReason, Long timeExpires, String formatExpires) {
		if (online) {
			Player player = plugin.getServer().getPlayer(playerName);

			playerName = player.getName();
			
			if (playerName.equals(mutedByName)) {
				Util.sendMessage(sender, plugin.banMessages.get("muteSelfError"));
				return;
			} else if (player.hasPermission("bm.exempt.tempmute")) {
				Util.sendMessage(sender, plugin.banMessages.get("muteExemptError"));
				return;
			}

		} else {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerName);

			playerName = offlinePlayer.getName();
		}

		if (plugin.mutedPlayersBy.containsKey(playerName)) {
			Util.sendMessage(sender, plugin.banMessages.get("alreadyMutedError").replace("[name]", playerName).replace("[displayName]", playerDisplayName));
		}

		plugin.addMute(playerName, reason, mutedByName, timeExpires);
		plugin.dbLogger.logTempMute(playerName, mutedByName, reason, timeExpires);

		String infoMessage = plugin.banMessages.get("playerTempMuted").replace("[expires]", formatExpires).replace("[name]", playerName).replace("[displayName]", playerDisplayName);

		plugin.logger.info(infoMessage);

		if (!sender.hasPermission("bm.notify"))
			Util.sendMessage(sender, infoMessage);

		String message = plugin.banMessages.get("tempMute").replace("[expires]", formatExpires).replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", mutedByName);
		Util.sendMessageWithPerm(message, "bm.notify");
		
		if(online) {
			// Inform the player they have been muted
			Player player = plugin.getServer().getPlayer(playerName);
			
			String mutedMessage = plugin.banMessages.get("tempMuted").replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", mutedByName);
			player.sendMessage(mutedMessage);
		}
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