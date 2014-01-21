package me.confuser.banmanager;

import org.bukkit.command.CommandSender;

/**
 * This is an class that will handle ban-manager's tool
 *
 * @author CraftThatBlock
 */
public interface BmTool {
	public void run(CommandSender sender, String[] args);

	public String name = null;
}
