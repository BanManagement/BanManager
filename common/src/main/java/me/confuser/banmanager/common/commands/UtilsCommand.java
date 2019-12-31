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
}
