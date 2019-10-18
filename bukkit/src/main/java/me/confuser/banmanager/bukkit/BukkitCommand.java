package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

public class BukkitCommand implements CommandExecutor, TabCompleter {

  private CommonCommand command;

  public BukkitCommand(CommonCommand command) {
    this.command = command;

    register();
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    CommonSender commonSender = getSender(sender);

    try {
      return this.command.onCommand(commonSender, this.command.getParser(args));
    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      e.printStackTrace();
    }

    return false;
  }

  private CommonSender getSender(CommandSender source) {
    if (source instanceof Player) {
      return new BukkitPlayer((Player) source, CommonCommand.getPlugin().getConfig().isOnlineMode());
    } else {
      return new BukkitSender(CommonCommand.getPlugin(), source);
    }
  }

  public void register() {
    Bukkit.getPluginCommand(command.getCommandName()).setExecutor(this);
  }

  @Override
  public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
    if (!this.command.isEnableTabCompletion()) return Collections.emptyList();

    return this.command.handlePlayerNameTabComplete(getSender(commandSender),args);
  }
}
