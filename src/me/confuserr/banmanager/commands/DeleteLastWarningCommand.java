package me.confuserr.banmanager.commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DeleteLastWarningCommand implements CommandExecutor {

	private BanManager plugin;

	public DeleteLastWarningCommand(BanManager instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;

			if (!player.hasPermission("bm.dwarn")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		if (plugin.usePartialNames()) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				remove(sender, target.getName());
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				if (!sender.hasPermission("bm.banoffline")) {
					Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
					return true;
				}

				remove(sender, args[0]);
			}
		} else {
			// Must be exact name
			if (plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				if (!sender.hasPermission("bm.banoffline")) {
					Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
					return true;
				}

				remove(sender, args[0]);
			} else {
				// Online
				Player target = plugin.getServer().getPlayerExact(args[0]);
				remove(sender, target.getName());
			}
		}

		return true;
	}

        private void remove(CommandSender sender, String playerName) {
                plugin.removeLastPlayerWarning(playerName);
                Util.sendMessage(sender, plugin.getMessage("playerLastWarningDeleted").replace("[name]", playerName));
        }
}
