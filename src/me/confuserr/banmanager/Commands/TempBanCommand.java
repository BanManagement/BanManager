package me.confuserr.banmanager.Commands;

import java.util.List;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempBanCommand implements CommandExecutor {

	private BanManager plugin;

	public TempBanCommand(BanManager instance) {
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
			if (!player.hasPermission("bm.tempban")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			} else {
				if (!player.hasPermission("bm.timelimit.bans.bypass")) {
					for (String k : plugin.timeLimitsBans.keySet()) {
						if (player.hasPermission("bm.timelimit.bans." + k)) {
							long timeLimit = getTimeStamp(plugin.timeLimitsBans.get(k));
							if (timeLimit < timeExpires) {
								// Erm, they tried to ban for too long
								Util.sendMessage(player, plugin.banMessages.get("banTimeLimitError"));
								return true;
							}
						}
					}
				}
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.banMessages.get("invalidPlayer"));
			return true;
		}

		if (timeExpires == 0) {
			Util.sendMessage(sender, plugin.banMessages.get("illegalDateError"));
			return true;
		}

		String reason = Util.getReason(args, 2);
		String viewReason = Util.viewReason(reason);

		String formatExpires = plugin.formatDateDiff(timeExpires);
		timeExpires = timeExpires / 1000;

		if (plugin.usePartialNames) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				ban(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason, timeExpires, formatExpires);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				if (!sender.hasPermission("bm.tempbanoffline")) {
					Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
					return true;
				}

				ban(sender, args[0], args[0], playerName, false, reason, viewReason, timeExpires, formatExpires);
			}
		} else {
			// Must be exact name
			if (plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				if (!sender.hasPermission("bm.tempbanoffline")) {
					Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
					return true;
				}

				ban(sender, args[0], args[0], playerName, false, reason, viewReason, timeExpires, formatExpires);
			} else {
				// Online
				Player target = plugin.getServer().getPlayerExact(args[0]);
				ban(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason, timeExpires, formatExpires);
			}
		}
		return true;
	}

	private void ban(CommandSender sender, String playerName, String playerDisplayName, String bannedByName, boolean online, String reason, String viewReason, Long timeExpires, String formatExpires) {
		if (online) {
			Player player = plugin.getServer().getPlayer(playerName);

			playerName = player.getName();

			if (playerName.equals(bannedByName)) {
				Util.sendMessage(sender, plugin.banMessages.get("banSelfError"));
				return;
			} else if (!sender.hasPermission("bm.exempt.override.tempban") && player.hasPermission("bm.exempt.tempban")) {
				Util.sendMessage(sender, plugin.banMessages.get("banExemptError"));
				return;
			} else if (plugin.bukkitBan) {
				if (player.isBanned()) {
					Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
					return;
				}
			} else if (plugin.bannedPlayers.contains(playerName.toLowerCase())) {
				Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
				return;
			}

			String kick = plugin.banMessages.get("tempBanKick").replace("[name]", player.getDisplayName()).replace("[reason]", viewReason).replace("[by]", bannedByName).replace("[expires]", formatExpires);
			player.kickPlayer(kick);

			if (plugin.bukkitBan)
				player.setBanned(true);
		} else {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerName);

			playerName = offlinePlayer.getName();

			if (plugin.bukkitBan) {
				if (offlinePlayer.isBanned()) {
					Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
					return;
				}
			} else if (plugin.bannedPlayers.contains(playerName.toLowerCase())) {
				Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
				return;
			}

			if (plugin.bukkitBan)
				offlinePlayer.setBanned(true);
		}

		plugin.dbLogger.logTempBan(playerName, bannedByName, reason, timeExpires);

		String infoMessage = plugin.banMessages.get("playerTempBanned").replace("[expires]", formatExpires).replace("[name]", playerName).replace("[displayName]", playerDisplayName);

		plugin.logger.info(infoMessage);

		if (!sender.hasPermission("bm.notify"))
			Util.sendMessage(sender, infoMessage);

		String message = plugin.banMessages.get("tempBan").replace("[expires]", formatExpires).replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", bannedByName);
		Util.sendMessageWithPerm(message, "bm.notify");
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