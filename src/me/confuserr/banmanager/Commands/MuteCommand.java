package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteCommand implements CommandExecutor {

	private BanManager plugin;

	public MuteCommand(BanManager instance) {
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
			if (!player.hasPermission("bm.mute")) {
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
				mute(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				mute(sender, args[0], args[0], playerName, false, reason, viewReason);
			}
		} else {
			// Must be exact name
			if(plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				mute(sender, args[0], args[0], playerName, false, reason, viewReason);
			} else {
				// Online
				Player target = plugin.getServer().getPlayerExact(args[0]);
				mute(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason);
			}
		}
		return true;
	}

	private void mute(CommandSender sender, String playerName, String playerDisplayName, String mutedByName, boolean online, String reason, String viewReason) {
		if (online) {
			Player player = plugin.getServer().getPlayer(playerName);

			if (playerName.equals(mutedByName)) {
				Util.sendMessage(sender, plugin.banMessages.get("muteSelfError"));
				return;
			} else if (player.hasPermission("bm.exempt.mute")) {
				Util.sendMessage(sender, plugin.banMessages.get("muteExemptError"));
				return;
			}

		}

		if (plugin.mutedPlayersBy.containsKey(playerName)) {
			Util.sendMessage(sender, plugin.banMessages.get("alreadyMutedError").replace("[name]", playerName).replace("[displayName]", playerDisplayName));
		}

		plugin.addMute(playerName, reason, mutedByName, (long) 0);
		plugin.dbLogger.logMute(playerName, mutedByName, reason);

		String infoMessage = plugin.banMessages.get("playerMuted").replace("[name]", playerName).replace("[displayName]", playerDisplayName);

		plugin.logger.info(infoMessage);

		if (!sender.hasPermission("bm.notify"))
			Util.sendMessage(sender, infoMessage);

		String message = plugin.banMessages.get("mute").replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", mutedByName);
		Util.sendMessageWithPerm(message, "bm.notify");
		
		if(online) {
			// Inform the player they have been muted
			Player player = plugin.getServer().getPlayer(playerName);
			
			String mutedMessage = plugin.banMessages.get("muted").replace("[reason]", viewReason).replace("[by]", mutedByName);
			player.sendMessage(mutedMessage);
		}
	}
}
