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
		String playerName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.warn")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		List<Player> list = plugin.getServer().matchPlayer(args[0]);
		if (list.size() == 1) {
			Player target = list.get(0);
			if (target.getName().equals(playerName)) {
				Util.sendMessage(sender, plugin.getMessage("warnSelfError"));
			} else if (!sender.hasPermission("bm.exempt.override.warn") && target.hasPermission("bm.exempt.warn")) {
				Util.sendMessage(sender, plugin.getMessage("warnExemptError"));
			} else {

				String reason = Util.getReason(args, 1);
				String viewReason = Util.viewReason(reason);

				plugin.dbLogger.logWarning(target.getName(), playerName, reason);

				String infoMessage = plugin.getMessage("playerWarned").replace("[name]", target.getName()).replace("[displayName]", target.getDisplayName());

				plugin.getLogger().info(infoMessage);

				if (!sender.hasPermission("bm.notify"))
					Util.sendMessage(sender, infoMessage);

				String message = plugin.getMessage("playerWarned").replace("[displayName]", target.getDisplayName()).replace("[name]", target.getName()).replace("[reason]", viewReason).replace("[by]", playerName);
				Util.sendMessageWithPerm(message, "bm.notify");
				
				Util.sendMessage(target, plugin.getMessage("warned").replace("[displayName]", target.getDisplayName()).replace("[name]", target.getName()).replace("[reason]", viewReason).replace("[by]", playerName));
			}
		} else if (list.size() > 1) {
			Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
			return false;
		} else {
			Util.sendMessage(sender, plugin.getMessage("playerNotOnline"));
			return false;
		}

		return true;
	}

}
