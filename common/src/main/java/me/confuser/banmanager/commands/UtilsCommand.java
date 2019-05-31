package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.commands.utils.MissingPlayersSubCommand;
import me.confuser.bukkitutil.commands.MultiCommandHandler;

public class UtilsCommand extends MultiCommandHandler<BanManager> {

  public UtilsCommand() {
    super("bmutils");

    registerCommands();
  }

  @Override
  public void registerCommands() {
    registerSubCommand(new MissingPlayersSubCommand());
  }

//  @Override
//  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
//    if (args.length == 0 && sender instanceof Player) return getCommands().get("list").onCommand(sender, args);
//
//    return super.onCommand(sender, command, commandName, args);
//  }
}
