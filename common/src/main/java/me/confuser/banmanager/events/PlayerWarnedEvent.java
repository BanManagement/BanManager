package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerWarnData;

public class PlayerWarnedEvent extends SilentEvent {

  @Getter
  private PlayerWarnData warning;

  public PlayerWarnedEvent(PlayerWarnData warning, boolean silent) {
    super(silent, true);
    this.warning = warning;
  }
}
