package me.confuser.banmanager.common.commands;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;

public abstract class CommonSubCommand {

  @Getter
  private BanManagerPlugin plugin;
  @Getter
  private String name;

  public CommonSubCommand(BanManagerPlugin plugin, String name) {
    this.plugin = plugin;
    this.name = name.toLowerCase();
  }

  public abstract boolean onCommand(CommonSender sender, CommandParser args);

  public abstract String getHelp();

  public abstract String getPermission();
}
