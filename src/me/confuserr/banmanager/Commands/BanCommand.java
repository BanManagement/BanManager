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

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 2)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.ban")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}
		
		if(!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		String reason = Util.getReason(args, 1);
		String viewReason = Util.viewReason(reason);

		if (plugin.usePartialNames()) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				ban(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				if (!sender.hasPermission("bm.banoffline")) {
					Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
					return true;
				}

				ban(sender, args[0], args[0], playerName, false, reason, viewReason);
			}
		} else {
			// Must be exact name
			if (plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				if (!sender.hasPermission("bm.banoffline")) {
					Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
					return true;
				}

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

			playerName = player.getName();

			if (playerName.equals(bannedByName)) {
				Util.sendMessage(sender, plugin.getMessage("banSelfError"));
				return;
			} else if (!sender.hasPermission("bm.exempt.override.ban") && player.hasPermission("bm.exempt.ban")) {
				Util.sendMessage(sender, plugin.getMessage("banExemptError"));
				return;
			} else if (plugin.useBukkitBans()) {
				if (player.isBanned()) {
					Util.sendMessage(sender, plugin.getMessage("alreadyBannedError").replace("[name]", playerName));
					return;
				}
			} else if (plugin.getPlayerBans().get(playerName.toLowerCase()) != null) {
				Util.sendMessage(sender, plugin.getMessage("alreadyBannedError").replace("[name]", playerName));
				return;
			}

			String kick = plugin.getMessage("banKick").replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", bannedByName);
			player.kickPlayer(kick);

			if (plugin.useBukkitBans())
				player.setBanned(true);
		} else {
			OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerName);

			playerName = offlinePlayer.getName();

			if (plugin.useBukkitBans()) {
				if (offlinePlayer.isBanned()) {
					Util.sendMessage(sender, plugin.getMessage("alreadyBannedError").replace("[name]", playerName));
					return;
				}
			} else if (plugin.getPlayerBans().get(playerName.toLowerCase()) != null) {
				Util.sendMessage(sender, plugin.getMessage("alreadyBannedError").replace("[name]", playerName));
				return;
			}

			if (plugin.useBukkitBans())
				offlinePlayer.setBanned(true);
		}

		plugin.addPlayerBan(playerName, bannedByName, reason);

		String infoMessage = plugin.getMessage("playerBanned").replace("[name]", playerName).replace("[displayName]", playerDisplayName);

		plugin.getLogger().info(infoMessage);

		if (!sender.hasPermission("bm.notify"))
			Util.sendMessage(sender, infoMessage);

		String message = plugin.getMessage("ban").replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", bannedByName);
		Util.sendMessageWithPerm(message, "bm.notify");
	}
}
