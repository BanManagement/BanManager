package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerWarnData;

public class PlayerWarnEvent extends SilentCancellableEvent {

  @Getter
  private PlayerWarnData warning;

  public PlayerWarnEvent(PlayerWarnData warning, boolean silent) {
    super(silent, true);
    this.warning = warning;
  }
}
