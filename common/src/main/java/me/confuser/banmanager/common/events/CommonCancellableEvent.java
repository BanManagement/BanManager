package me.confuser.banmanager.common.events;

import lombok.Getter;
import lombok.Setter;

public abstract class CommonCancellableEvent extends CommonEvent {

  @Getter
  @Setter
  private boolean cancelled = false;

  public CommonCancellableEvent(boolean isAsync) {
    super(isAsync);
  }
}
