package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BanCommand implements CommandExecutor {

	private BanManager plugin;

	public BanCommand(BanManager instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 2)
			return false;

		Player player = null;
		String playerName = plugin.banMessages.get("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.ban")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}

		String reason = Util.getReason(args, 1);
		String viewReason = Util.viewReason(reason);

		if (plugin.usePartialNames) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				ban(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				ban(sender, args[0], args[0], playerName, false, reason, viewReason);
			}
		} else {
			// Must be exact name
			if (plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				ban(sender, args[0], args[0], playerName, false, reason, viewReason);
			} else {
				// Online
				Player target = plugin.getServer().getPlayerExact(args[0]);
				ban(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason);
			}
		}
		return true;
	}

	private void ban(CommandSender sender, String playerName, String playerDisplayName, String bannedByName, boolean online, String reason, String viewReason) {
		if (online) {
			Player player = plugin.getServer().getPlayer(playerName);

			if (playerName.equals(bannedByName)) {
				Util.sendMessage(sender, plugin.banMessages.get("banSelfError"));
				return;
			} else if (player.hasPermission("bm.exempt.ban")) {
				Util.sendMessage(sender, plugin.banMessages.get("banExemptError"));
				return;
			} else if (plugin.bukkitBan) {
				if (player.isBanned()) {
					Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
					return;
				}
			} else if (plugin.bannedPlayers.contains(playerName)) {
				Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
				return;
			}

			String kick = plugin.banMessages.get("banKick").replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", bannedByName);
			player.kickPlayer(kick);

			if (plugin.bukkitBan)
				player.setBanned(true);
		} else {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerName);

			if (plugin.bukkitBan) {
				if (offlinePlayer.isBanned()) {
					Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
					return;
				}
			} else if (plugin.bannedPlayers.contains(playerName)) {
				Util.sendMessage(sender, plugin.banMessages.get("alreadyBannedError").replace("[name]", playerName));
				return;
			}

			if (plugin.bukkitBan)
				offlinePlayer.setBanned(true);
		}

		plugin.dbLogger.logBan(playerName, bannedByName, reason);

		String infoMessage = plugin.banMessages.get("playerBanned").replace("[name]", playerName).replace("[displayName]", playerDisplayName);

		plugin.logger.info(infoMessage);

		if (!sender.hasPermission("bm.notify"))
			Util.sendMessage(sender, infoMessage);

		String message = plugin.banMessages.get("ban").replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", bannedByName);
		Util.sendMessageWithPerm(message, "bm.notify");
	}
}
