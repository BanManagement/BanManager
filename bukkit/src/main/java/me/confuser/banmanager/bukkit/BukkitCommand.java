package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class BukkitCommand implements CommandExecutor {

  private CommonCommand command;

  public BukkitCommand(CommonCommand command) {
    this.command = command;

    register();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    CommonSender commonSender = new BukkitSender(CommonCommand.getPlugin(), sender);

    return this.command.onCommand(commonSender, new CommandParser(CommonCommand.getPlugin(), args));
  }

  public void register() {
    Bukkit.getPluginCommand(command.getCommandName()).setExecutor(this);
  }
}
