package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnMuteAllCommand implements CommandExecutor {

	private BanManager plugin;

	public UnMuteAllCommand(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 1)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.unmuteall")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(args[0]);
		final String offlineName = offlinePlayer.getName();
		final String byName = playerName;

		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

			public void run() {
				if (!plugin.isPlayerMuted(offlineName))
					Util.sendMessage(sender, plugin.getMessage("playerNotMutedError"));
				else {
					if (sender.hasPermission("bm.unmute.by")) {
						if (!plugin.getPlayerMute(offlineName).getBy().equals(byName) && !sender.hasPermission("bm.exempt.override.mute")) {
							Util.sendMessage(sender, plugin.getMessage("commandPermissionError"));
							return;
						}
					}

					plugin.removeExternalPlayerMute(offlineName, byName);

					String message = plugin.getMessage("playerUnmuted").replace("[name]", offlineName).replace("[by]", byName);

					plugin.getLogger().info(message);

					if (!sender.hasPermission("bm.notify.unmute"))
						Util.sendMessage(sender, message);

					Util.sendMessageWithPerm(message, "bm.notify.unmute");
				}
			}
		});

		return true;
	}
}