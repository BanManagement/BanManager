package me.confuserr.banmanager.Commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearCommand implements CommandExecutor {

	private BanManager plugin;
	private HashSet<String> types = new HashSet<String>();

	public ClearCommand(BanManager instance) {
		plugin = instance;

		types.add("banrecords");
		types.add("muterecords");
		types.add("kicks");
		types.add("warnings");
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;

			if (!player.hasPermission("bm.clear")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		ArrayList<String> type = new ArrayList<String>();

		if (args.length == 1) {
			// Clear everything!
			type.addAll(types);
		} else if (args.length == 2) {
			// Remove specific things
			if (!types.contains(args[1].toLowerCase())) {
				Util.sendMessage(sender, plugin.getMessage("invalidClearType"));
			} else
				type.add(args[1].toLowerCase());
		}

		if (plugin.usePartialNames()) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				clear(sender, target.getName(), type);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				if (!sender.hasPermission("bm.banoffline")) {
					Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
					return true;
				}

				clear(sender, args[0], type);
			}
		} else {
			// Must be exact name
			if (plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				if (!sender.hasPermission("bm.banoffline")) {
					Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
					return true;
				}

				clear(sender, args[0], type);
			} else {
				// Online
				Player target = plugin.getServer().getPlayerExact(args[0]);
				clear(sender, target.getName(), type);
			}
		}

		return true;
	}

	private void clear(CommandSender sender, String playerName, ArrayList<String> types) {
		for (String type : types) {
			if (type.equals("banrecords")) {
				plugin.removePlayerBanRecords(playerName);
			} else if (type.equals("muterecords")) {
				plugin.removePlayerMuteRecords(playerName);
			} else if (type.equals("kicks")) {
				plugin.removePlayerKickRecords(playerName);
			} else if (type.equals("warnings")) {
				plugin.removePlayerWarnings(playerName);
			}

			Util.sendMessage(sender, plugin.getMessage("playerRecordCleared").replace("[recordType]", type).replace("[name]", playerName));
		}
	}
}
