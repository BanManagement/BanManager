package me.confuser.banmanager.events;

import lombok.Getter;
import lombok.Setter;

public abstract class SilentCancellableEvent extends CustomCancellableEvent {
  @Getter
  @Setter
  private boolean silent = false;

  public SilentCancellableEvent(boolean silent, boolean isAsync) {
    super(isAsync);

    this.silent = silent;
  }

}
