package me.confuserr.banmanager.Commands;

import java.util.List;

import me.confuserr.banmanager.BanManager;
import me.confuserr.banmanager.Util;
import me.confuserr.banmanager.data.BanData;
import me.confuserr.banmanager.data.MuteData;

import org.bukkit.ChatColor;
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
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, final String args[]) {

		if (args.length != 1)
			return false;

		Player player = null;

		if (sender instanceof Player) {
			player = (Player) sender;
			if (!player.hasPermission("bm.bminfo")) {
				Util.sendMessage(player, plugin.getMessage("commandPermissionError"));
				return true;
			}
		}

		if (!Util.isValidPlayerName(args[0])) {
			Util.sendMessage(sender, plugin.getMessage("invalidPlayer"));
			return true;
		}

		plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {

			public void run() {
				String name = "";
				if (!plugin.usePartialNames())
					name = args[0];
				else {
					List<Player> list = plugin.getServer().matchPlayer(args[0]);
					if (list.size() == 1)
						name = list.get(0).getName();
					else if (list.size() > 1) {
						Util.sendMessage(sender, plugin.getMessage("multiplePlayersFoundError"));
						return;
					} else {
						// Possible offline player
						name = args[0];
					}
				}

				name = name.toLowerCase();
				String message = plugin.getMessage("bmInfo").replace("[name]", name);

				if (plugin.isPlayerBanned(name)) {
					BanData banData = plugin.getPlayerBan(name);

					String banMessage = Util.viewReason(banData.getReason()) + "\n" + ChatColor.RED + "Banned By: " + banData.getBy();

					String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(banData.getTime() * 1000));
					banMessage += "\n" + ChatColor.RED + "Banned at: " + date;

					if (banData.getExpires() == 0)
						banMessage += "\n" + ChatColor.RED + "Expires: Never";
					else {
						// Temp ban, check to see if expired
						if ((System.currentTimeMillis() / 1000) < banData.getExpires()) {
							// Still banned
							banMessage += "\n" + ChatColor.RED + "Expires in: " + Util.formatDateDiff((long) banData.getExpires() * 1000);
						} else
							banMessage += "\n" + ChatColor.RED + "Expires in: Now";
					}

					message = message.replace("[currentBan]", banMessage);
				} else
					message = message.replace("[currentBan]", "None");

				message = message.replace("[previousBans]", Integer.toString(plugin.dbLogger.getPastBanCount(name)));

				if (plugin.isPlayerMuted(name)) {
					MuteData muteData = plugin.getPlayerMute(name);

					String muteMessage = Util.viewReason(muteData.getReason()) + "\n" + ChatColor.RED + "Muted By: " + muteData.getBy();

					String date = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date(muteData.getTime() * 1000));
					muteMessage += "\n" + ChatColor.RED + "Muted at: " + date;

					if (muteData.getExpires() == 0)
						muteMessage += "\n" + ChatColor.RED + "Expires: Never";
					else {
						// Temp ban, check to see if expired
						if ((System.currentTimeMillis() / 1000) < muteData.getExpires()) {
							// Still banned
							muteMessage += "\n" + ChatColor.RED + "Expires in: " + Util.formatDateDiff((long) muteData.getExpires() * 1000);
						} else
							muteMessage += "\n" + ChatColor.RED + "Expires in: Now";
					}

					message = message.replace("[currentMute]", muteMessage);
				} else {
					message = message.replace("[currentMute]", "None");
				}

				message = message.replace("[previousMutes]", Integer.toString(plugin.dbLogger.getPastMuteCount(name)));

				message = message.replace("[kicksCount]", Integer.toString(plugin.dbLogger.getKickCount(name)));

				message = message.replace("[warningsCount]", Integer.toString(plugin.dbLogger.getWarningCount(name)));

				Util.sendMessage(sender, message);
			}
		}, 1L);
		return true;
	}
}