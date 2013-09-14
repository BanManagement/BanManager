package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MuteAllCommand implements CommandExecutor {

	private BanManager plugin;

	public MuteAllCommand(BanManager instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 2)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.muteall")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		String reason = Util.getReason(args, 1);
		String viewReason = Util.viewReason(reason);

		if (plugin.usePartialNames()) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				mute(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				mute(sender, args[0], args[0], playerName, false, reason, viewReason);
			}
		} else {
			// Must be exact name
			if (plugin.getServer().getPlayerExact(args[0]) == null) {
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

	@SuppressWarnings("deprecation")
	private void mute(final CommandSender sender, final String playerName, final String playerDisplayName, final String mutedByName, final boolean online, final String reason, final String viewReason) {
		if (online) {
			Player player = plugin.getServer().getPlayer(playerName);

			if (playerName.equals(mutedByName)) {
				Util.sendMessage(sender, plugin.getMessage("muteSelfError"));
				return;
			} else if (!sender.hasPermission("bm.exempt.override.mute") && player.hasPermission("bm.exempt.mute")) {
				Util.sendMessage(sender, plugin.getMessage("muteExemptError"));
				return;
			}

		}

		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

			public void run() {
				if (plugin.isPlayerMuted(playerName)) {
					Util.sendMessage(sender, plugin.getMessage("alreadyMutedError").replace("[name]", playerName).replace("[displayName]", playerDisplayName));
					return;
				}

				plugin.addExternalPlayerMute(playerName, mutedByName, reason);

				String infoMessage = plugin.getMessage("playerMuted").replace("[name]", playerName).replace("[displayName]", playerDisplayName);

				plugin.getLogger().info(infoMessage);

				if (!sender.hasPermission("bm.notify.mute"))
					Util.sendMessage(sender, infoMessage);

				String message = plugin.getMessage("mute").replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", mutedByName);
				Util.sendMessageWithPerm(message, "bm.notify.mute");

				if (online) {
					// Inform the player they have been muted
					// Not sure if below is thread safe or not, experimental!
					Player player = plugin.getServer().getPlayer(playerName);

					String mutedMessage = plugin.getMessage("muted").replace("[reason]", viewReason).replace("[by]", mutedByName);
					player.sendMessage(mutedMessage);
				}
			}
		});
	}
}
