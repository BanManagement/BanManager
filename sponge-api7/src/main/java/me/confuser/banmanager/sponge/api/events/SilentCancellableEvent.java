package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import lombok.Setter;

public abstract class SilentCancellableEvent extends CustomCancellableEvent {

  @Getter
  @Setter
  private boolean silent;

  public SilentCancellableEvent(boolean silent) {
    super();

    this.silent = silent;
  }

}
