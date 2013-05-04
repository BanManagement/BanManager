package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BmInfoCommand implements CommandExecutor {

	private BanManager plugin;

	public BmInfoCommand(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, final String args[]) {

		if (args.length != 1)
			return false;

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;
			if (!player.hasPermission("bm.bminfo")) {
				Util.sendMessage(player, plugin.banMessages.get("commandPermissionError"));
				return true;
			}
		}
		
		if(!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.banMessages.get("invalidPlayer"));
			return true;
		}

		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

			public void run() {
				if (!plugin.usePartialNames) {
					Util.sendMessage(sender, plugin.banMessages.get("bmInfo").replace("[name]", args[0]).replace("[currentBan]", plugin.dbLogger.getCurrentBanInfo(args[0])).replace("[previousBans]", Integer.toString(plugin.dbLogger.getPastBanCount(args[0]))).replace("[currentMute]", plugin.dbLogger.getCurrentMuteInfo(args[0])).replace("[previousMutes]", Integer.toString(plugin.dbLogger.getPastMuteCount(args[0]))).replace("[warningsCount]", Integer.toString(plugin.dbLogger.getWarningCount(args[0]))));
				} else {
					List<Player> list = plugin.getServer().matchPlayer(args[0]);
					if (list.size() == 1) {
						String target = list.get(0).getName();
						Util.sendMessage(sender, plugin.banMessages.get("bmInfo").replace("[name]", target).replace("[currentBan]", plugin.dbLogger.getCurrentBanInfo(target)).replace("[previousBans]", Integer.toString(plugin.dbLogger.getPastBanCount(target))).replace("[currentMute]", plugin.dbLogger.getCurrentMuteInfo(target)).replace("[previousMutes]", Integer.toString(plugin.dbLogger.getPastMuteCount(target))).replace("[warningsCount]", Integer.toString(plugin.dbLogger.getWarningCount(args[0]))));
					} else if (list.size() > 1) {
						Util.sendMessage(sender, plugin.banMessages.get("multiplePlayersFoundError"));
					} else {
						// Possible offline player
						String target = args[0];
						Util.sendMessage(sender, plugin.banMessages.get("bmInfo").replace("[name]", target).replace("[currentBan]", plugin.dbLogger.getCurrentBanInfo(target)).replace("[previousBans]", Integer.toString(plugin.dbLogger.getPastBanCount(target))).replace("[currentMute]", plugin.dbLogger.getCurrentMuteInfo(target)).replace("[previousMutes]", Integer.toString(plugin.dbLogger.getPastMuteCount(target))).replace("[warningsCount]", Integer.toString(plugin.dbLogger.getWarningCount(args[0]))));
					}
				}
			}
		}, 1L);
		return true;
	}
}