package me.confuser.banmanager.sponge.api.events;

import lombok.Getter;
import lombok.Setter;

public abstract class SilentEvent extends CustomEvent {
  @Getter
  @Setter
  private boolean silent;

  public SilentEvent(boolean silent) {
    super();
    this.silent = silent;
  }

}
