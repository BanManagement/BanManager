package me.confuser.banmanager.tools;

import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.BmTool;
import me.confuser.banmanager.data.BanData;
import me.confuser.banmanager.data.IPBanData;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class ListBansTool implements BmTool {

	public String name = "ListBans";

	@Override
	public void run(CommandSender sender, String[] args) {
		if (sender.hasPermission("bm.tools.listbans")) {
			String type = "all";

			if (args.length == 1) {
				type = args[0];
			}

			if (type.equals("all") || type.equals("players")) {
				String bannedList = "";
				for (Map.Entry<String, BanData> banned : BmAPI.getBanManager().getPlayerBans().entrySet()) {
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
				for (Map.Entry<String, IPBanData> banned : BmAPI.getBanManager().getIPBans().entrySet()) {
					bannedList += banned.getKey() + ", ";
				}

				sender.sendMessage(ChatColor.UNDERLINE + "Banned IPs List");
				if (bannedList.length() > 0)
					sender.sendMessage(bannedList.substring(0, bannedList.length() - 2));
				else
					sender.sendMessage("None");
			}
		}
	}
}
