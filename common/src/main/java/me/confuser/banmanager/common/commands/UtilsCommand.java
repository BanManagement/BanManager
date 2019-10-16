package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.utils.MissingPlayersSubCommand;

public class UtilsCommand extends MultiCommonCommand {

  public UtilsCommand(BanManagerPlugin plugin) {
    super(plugin, "bmutils");

    registerCommands();
  }

  @Override
  public void registerCommands() {
    registerCommonSubCommand(new MissingPlayersSubCommand(getPlugin()));
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length == 0 && !sender.isConsole()) return getCommands().get("list").onCommand(sender, parser);

    return super.onCommand(sender, parser);
  }
}
