package me.confuserr.banmanager.Commands;

import java.util.List;
import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TempMuteAllCommand implements CommandExecutor {

	private BanManager plugin;

	public TempMuteAllCommand(BanManager instance) {
		plugin = instance;
	}

	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String args[]) {
		if (args.length < 3)
			return false;

		Player player = null;
		String playerName = plugin.getMessage("consoleName");

		Long timeExpires = Util.getTimeStamp(args[1]);

		if (sender instanceof Player) {
			player = (Player) sender;
			playerName = player.getName();
			if (!player.hasPermission("bm.tempmuteall")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			} else {
				if (!player.hasPermission("bm.timelimit.mutes.bypass")) {
					for (String k : plugin.getTimeLimitsMutes().keySet()) {
						if (player.hasPermission("bm.timelimit.mutes." + k)) {
							long timeLimit = Util.getTimeStamp(plugin.getTimeLimitsMutes().get(k));
							if (timeLimit < timeExpires) {
								// Erm, they tried to ban for too long
								Util.sendMessage(player, plugin.getMessage("muteTimeLimitError"));
								return true;
							}
						}
					}
				}
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		String reason = Util.getReason(args, 2);
		String viewReason = Util.viewReason(reason);

		if (timeExpires == 0) {
			Util.sendMessage(sender, plugin.getMessage("illegalDateError"));
			return true;
		}
		timeExpires = timeExpires / 1000;
		String formatExpires = Util.formatDateDiff(timeExpires * 1000);

		if (plugin.usePartialNames()) {
			List<Player> list = plugin.getServer().matchPlayer(args[0]);
			if (list.size() == 1) {
				Player target = list.get(0);
				mute(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason, timeExpires, formatExpires);
			} else if (list.size() > 1) {
				Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
				return false;
			} else {
				// Offline
				mute(sender, args[0], args[0], playerName, false, reason, viewReason, timeExpires, formatExpires);
			}
		} else {
			// Must be exact name
			if (plugin.getServer().getPlayerExact(args[0]) == null) {
				// Offline player
				mute(sender, args[0], args[0], playerName, false, reason, viewReason, timeExpires, formatExpires);
			} else {
				// Online
				Player target = plugin.getServer().getPlayerExact(args[0]);
				mute(sender, target.getName(), target.getDisplayName(), playerName, true, reason, viewReason, timeExpires, formatExpires);
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	private void mute(final CommandSender sender, final String playerName, final String playerDisplayName, final String mutedByName, final boolean online, final String reason, final String viewReason, final Long timeExpires, final String formatExpires) {
		if (online) {
			Player player = plugin.getServer().getPlayer(playerName);

			if (playerName.equals(mutedByName)) {
				Util.sendMessage(sender, plugin.getMessage("muteSelfError"));
				return;
			} else if (!sender.hasPermission("bm.exempt.override.tempmute") && player.hasPermission("bm.exempt.tempmute")) {
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

				plugin.addExternalPlayerMute(playerName, mutedByName, reason, timeExpires);

				String infoMessage = plugin.getMessage("playerTempMuted").replace("[expires]", formatExpires).replace("[name]", playerName).replace("[displayName]", playerDisplayName);

				plugin.getServer().getConsoleSender().sendMessage(infoMessage);

				if (!sender.hasPermission("bm.notify.tempmute"))
					Util.sendMessage(sender, infoMessage);

				String message = plugin.getMessage("tempMute").replace("[expires]", formatExpires).replace("[displayName]", playerDisplayName).replace("[name]", playerName).replace("[reason]", viewReason).replace("[by]", mutedByName);
				Util.sendMessageWithPerm(message, "bm.notify.tempmute");

				if (online) {
					// Inform the player they have been muted
					// Not sure if below is thread safe or not, experimental!
					Player player = plugin.getServer().getPlayer(playerName);

					String mutedMessage = plugin.getMessage("tempMuted").replace("[expires]", formatExpires).replace("[reason]", viewReason).replace("[by]", mutedByName);
					player.sendMessage(mutedMessage);
				}
			}
		});
	}
}