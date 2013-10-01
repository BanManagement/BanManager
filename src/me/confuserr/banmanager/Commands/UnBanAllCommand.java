package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnBanAllCommand implements CommandExecutor {

	private BanManager plugin;

	public UnBanAllCommand(BanManager instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 1)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.unbanall")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
		if (!plugin.isPlayerBanned(offlinePlayer.getName().toLowerCase())) {
			Util.sendMessage(sender, plugin.getMessage("unbanError"));
		} else {
			final String offlineName = offlinePlayer.getName();
			
			if (sender.hasPermission("bm.unban.by")) {
				if (!plugin.getPlayerBan(offlineName).getBy().equals(playerName) && !sender.hasPermission("bm.exempt.override.ban")) {
					Util.sendMessage(sender, plugin.getMessage("commandPermissionError"));
					return true;
				}
			}

			plugin.removeExternalPlayerBan(offlineName, playerName);

			String message = plugin.getMessage("playerUnbanned").replace("[name]", offlineName).replace("[by]", playerName);

			plugin.getServer().getConsoleSender().sendMessage(message);

			if (!sender.hasPermission("bm.notify.unban"))
				Util.sendMessage(sender, message);

			Util.sendMessageWithPerm(message, "bm.notify.unban");
		}
		return true;
	}

}