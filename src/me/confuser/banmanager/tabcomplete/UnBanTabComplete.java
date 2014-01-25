package me.confuser.banmanager.tabcomplete;

import java.util.ArrayList;
import java.util.List;

import me.confuser.banmanager.BanManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class UnBanTabComplete implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String commandLabel, String args[]) {		
		ArrayList<String> mostLike = new ArrayList<String>();

		if (args.length == 1) {
			for (String playerName : BanManager.getPlugin().getPlayerBans().keySet()) {
				if (playerName.startsWith(args[0]))
					mostLike.add(playerName);
			}
		}

		return mostLike;
	}

}
