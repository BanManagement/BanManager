package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnMuteCommand implements CommandExecutor {

	private BanManager plugin;

	public UnMuteCommand(BanManager instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 1)
			return false;

		Player player = null;
		String playerName = plugin.banMessages.get("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.unmute")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		if(!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.banMessages.get("invalidPlayer"));
			return true;
		}
		
		OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
		String offlineName = offlinePlayer.getName();

		if (!plugin.dbLogger.isMuted(offlineName)) {
			Util.sendMessage(sender, plugin.banMessages.get("playerNotMutedError"));
		} else {
			plugin.removeMute(offlineName, playerName);

			String message = plugin.banMessages.get("playerUnmuted").replace("[name]", offlineName).replace("[by]", playerName);

			plugin.logger.info(message);

			if (!sender.hasPermission("bm.notify"))
				Util.sendMessage(sender, message);

			Util.sendMessageWithPerm(message, "bm.notify");
		}
		return true;
	}

}