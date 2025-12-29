package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerWarnData;


public class PlayerWarnedEvent extends SilentEvent {

  @Getter
  private PlayerWarnData warning;

  public PlayerWarnedEvent(PlayerWarnData warning, boolean silent) {
    super(silent);
    this.warning = warning;
  }
}
