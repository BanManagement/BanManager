package me.confuserr.banmanager.Commands;

import me.confuserr.banmanager.BanManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BmToolsCommand implements CommandExecutor {

	private BanManager plugin;

	public BmToolsCommand(BanManager instance) {
		plugin = instance;
	}

	@Override
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, final String args[]) {

		if (args.length == 0)
			return false;

		switch (args[0]) {
			case "listbans":
				if (sender.hasPermission("bm.tools.listbans")) {
					String type = "all";

					if (args.length == 2) {
						type = args[1];
					}

					if (type.equals("all") || type.equals("players")) {
						String bannedList = "";
						for (String banned : plugin.bannedPlayers) {
							bannedList += banned + ", ";
						}

						sender.sendMessage(ChatColor.UNDERLINE + "Banned Players List");
						if (bannedList.length() > 0)
							sender.sendMessage(bannedList.substring(0, bannedList.length() - 2));
						else
							sender.sendMessage("None");
					}

					if (type.equals("all") || type.equals("ips")) {
						String bannedList = "";
						for (String banned : plugin.bannedIps) {
							bannedList += banned + ", ";
						}

						sender.sendMessage(ChatColor.UNDERLINE + "Banned IPs List");
						if (bannedList.length() > 0)
							sender.sendMessage(bannedList.substring(0, bannedList.length() - 2));
						else
							sender.sendMessage("None");
					}
				}
			break;
		}

		return true;
	}

}
