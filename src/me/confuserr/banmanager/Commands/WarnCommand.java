package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WarnCommand implements CommandExecutor {

	private BanManager plugin;

	public WarnCommand(BanManager instance) {
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
			if (!player.hasPermission("bm.warn")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.banMessages.get("invalidPlayer"));
			return true;
		}

		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		if (list.size() == 1) {
			Player target = list.get(0);
			if (target.getName().equals(playerName)) {
				Util.sendMessage(sender, plugin.banMessages.get("warnSelfError"));
			} else if (!sender.hasPermission("bm.exempt.override.warn") && target.hasPermission("bm.exempt.warn")) {
				Util.sendMessage(sender, plugin.banMessages.get("warnExemptError"));
			} else {

				String reason = Util.getReason(args, 1);
				String viewReason = Util.viewReason(reason);

				plugin.dbLogger.logWarning(target.getName(), playerName, reason);

				String infoMessage = plugin.banMessages.get("playerWarned").replace("[name]", target.getName()).replace("[displayName]", target.getDisplayName());

				plugin.logger.info(infoMessage);

				if (!sender.hasPermission("bm.notify"))
					Util.sendMessage(sender, infoMessage);

				String message = plugin.banMessages.get("playerWarned").replace("[displayName]", target.getDisplayName()).replace("[name]", target.getName()).replace("[reason]", viewReason).replace("[by]", playerName);
				Util.sendMessageWithPerm(message, "bm.notify");
				
				Util.sendMessage(target, plugin.banMessages.get("warned").replace("[displayName]", target.getDisplayName()).replace("[name]", target.getName()).replace("[reason]", viewReason).replace("[by]", playerName));
			}
		} else if (list.size() > 1) {
			Util.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
			return false;
		} else {
			Util.sendMessage(sender, plugin.banMessages.get("playerNotOnline"));
			return false;
		}

		return true;
	}

}
