package me.confuser.banmanager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.MessagesConfig;
import me.confuser.bukkitutil.commands.BukkitCommand;

public class ReloadCommand extends BukkitCommand<BanManager> {
	public ReloadCommand() {
		super("bmreload");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String commandName, String[] args) {
		plugin.getDefaultConfig().load();
		new MessagesConfig().load();
		
		return true;
	}
}
