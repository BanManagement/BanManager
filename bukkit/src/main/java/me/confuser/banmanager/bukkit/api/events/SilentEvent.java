package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import lombok.Setter;

public abstract class SilentEvent extends CustomEvent {
  @Getter
  @Setter
  private boolean silent = false;

  public SilentEvent(boolean silent, boolean isAsync) {
    super(isAsync);
    this.silent = silent;
  }

}
