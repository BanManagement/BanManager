package me.confuser.banmanager.commands;

import me.confuser.banmanager.BmAPI;
import me.confuser.banmanager.BmTool;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BmToolsCommand implements CommandExecutor {

	@SuppressWarnings("deprecation")
	public boolean onCommand(final CommandSender sender, Command command, String commandLabel, final String args[]) {

		if (args.length == 0)
			return false;

		BmTool tool = BmAPI.getTool(args[0].toLowerCase());
		if (tool == null) {
			// Tool not found
			sender.sendMessage(ChatColor.RED + "[BanManager] Tool '" + args[0] + "' not found!");
		} else {
			tool.run(sender, args);
		}

		return true;
	}
}
