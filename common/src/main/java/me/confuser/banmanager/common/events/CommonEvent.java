package me.confuser.banmanager.common.events;

import lombok.Getter;

public abstract class CommonEvent {
  @Getter
  private final boolean isAsync;

  public CommonEvent(boolean isAsync) {
    this.isAsync = isAsync;
  }
}
