package me.confuser.banmanager.commands;

import java.util.Map.Entry;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.BanData;
import me.confuser.banmanager.data.IPBanData;
import net.gravitydevelopment.updater.Updater;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BmToolsCommand implements CommandExecutor {

	private BanManager plugin;

	public BmToolsCommand(BanManager instance) {
		plugin = instance;
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, final String args[]) {

		if (args.length == 0)
			return false;

		if (args[0].equals("listbans")) {
			if (sender.hasPermission("bm.tools.listbans")) {
				String type = "all";

				if (args.length == 2) {
					type = args[1];
				}

				if (type.equals("all") || type.equals("players")) {
					String bannedList = "";
					for (Entry<String, BanData> banned : plugin.getPlayerBans().entrySet()) {
						bannedList += banned.getKey() + ", ";
					}

					sender.sendMessage(ChatColor.UNDERLINE + "Banned Players List");
					if (bannedList.length() > 0)
						sender.sendMessage(bannedList.substring(0, bannedList.length() - 2));
					else
						sender.sendMessage("None");
				}

				if (type.equals("all") || type.equals("ips")) {
					String bannedList = "";
					for (Entry<String, IPBanData> banned : plugin.getIPBans().entrySet()) {
						bannedList += banned.getKey() + ", ";
					}

					sender.sendMessage(ChatColor.UNDERLINE + "Banned IPs List");
					if (bannedList.length() > 0)
						sender.sendMessage(bannedList.substring(0, bannedList.length() - 2));
					else
						sender.sendMessage("None");
				}
			}
		} else if (args[0].equals("update")) {
			if (sender.hasPermission("bm.tools.update")) {
				if (!plugin.checkForUpdates())
					sender.sendMessage(ChatColor.RED + "[BanManager] Please enable update checking within your config!");
				if (plugin.isUpdateAvailable()) {
					plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new Runnable() {
						public void run() {
							new Updater(plugin, 41473, plugin.jarFile, Updater.UpdateType.NO_VERSION_CHECK, true);

							sender.sendMessage(ChatColor.GREEN + "[BanManager] Updating, please restart the server in a moment to apply the update.");
						}
					});
				} else {
					sender.sendMessage(ChatColor.RED + "No updates available.");
				}
			}
		}

		return true;
	}
}
