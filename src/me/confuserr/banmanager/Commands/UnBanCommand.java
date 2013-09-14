package me.confuserr.banmanager.Commands;

import java.util.ArrayList;
import java.util.List;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class UnBanCommand implements CommandExecutor, TabCompleter {

	private BanManager plugin;

	public UnBanCommand(BanManager instance) {
		plugin = instance;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String args[]) {		
		ArrayList<String> mostLike = new ArrayList<String>();

		if (args.length == 1) {
			for (String playerName : plugin.getPlayerBans().keySet()) {
				if (playerName.startsWith(args[0]))
					mostLike.add(playerName);
			}
		}

		return mostLike;
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 1)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.unban")) {
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

			plugin.removePlayerBan(offlineName, playerName, true);

			String message = plugin.getMessage("playerUnbanned").replace("[name]", offlineName).replace("[by]", playerName);

			plugin.getLogger().info(message);

			if (!sender.hasPermission("bm.notify.unban"))
				Util.sendMessage(sender, message);

			Util.sendMessageWithPerm(message, "bm.notify.unban");
		}
		return true;
	}

}