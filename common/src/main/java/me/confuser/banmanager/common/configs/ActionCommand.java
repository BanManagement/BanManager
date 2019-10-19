package me.confuser.banmanager.common.configs;

import lombok.Getter;

public class ActionCommand {

  @Getter
  private final String command;
  @Getter
  private final long delay;

  public ActionCommand(String command, long delay) {
    this.command = command;
    this.delay = delay;
  }
}
