package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;

public class BmCommand extends MultiCommonCommand {

  public BmCommand(BanManagerPlugin plugin) {
    super(plugin, "bm");
    registerCommands();
  }

  @Override
  public void registerCommands() {
    registerCommonSubCommand(new DashboardSubCommand(getPlugin()));
  }
}
