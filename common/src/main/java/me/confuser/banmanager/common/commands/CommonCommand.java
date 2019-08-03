package me.confuser.banmanager.common.commands;

import lombok.Getter;

public abstract class CommonCommand {
  @Getter
  private final String permission;
  @Getter
  private final String commandName;

  public CommonCommand(String commandName) {
    this.commandName = commandName;
    this.permission = "bm.command." + commandName;
  }

  public abstract boolean onCommand(final CommonSender sender, CommandParser args);
}
