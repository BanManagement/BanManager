package me.confuser.banmanager.bungee.api.events;

import lombok.Getter;
import lombok.Setter;

public abstract class SilentEvent extends CustomEvent {
  @Getter
  @Setter
  private boolean silent = false;

  public SilentEvent(boolean silent) {
    super();

    this.silent = silent;
  }

}
