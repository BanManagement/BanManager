package me.confuser.banmanager.common.configs;

import lombok.Getter;

public class ActionCommand {

  @Getter
  private final String command;
  @Getter
  private final long delay;
  @Getter
  private final String pointsTimeframe;

  public ActionCommand(String command, long delay, String pointsTimeframe) {
    this.command = command;
    this.delay = delay;
    this.pointsTimeframe = pointsTimeframe;
  }
}
