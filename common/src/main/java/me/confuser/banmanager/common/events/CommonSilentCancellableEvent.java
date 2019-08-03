package me.confuser.banmanager.common.events;

import lombok.Getter;
import lombok.Setter;

public abstract class CommonSilentCancellableEvent extends CommonSilentEvent {

  @Getter
  @Setter
  private boolean cancelled = false;

  public CommonSilentCancellableEvent(boolean silent, boolean isAsync) {
    super(silent, isAsync);
  }
}
