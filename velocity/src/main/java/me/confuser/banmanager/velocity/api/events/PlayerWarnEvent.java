package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerWarnData;


public class PlayerWarnEvent extends SilentEvent {

  @Getter
  private PlayerWarnData warning;

  public PlayerWarnEvent(PlayerWarnData warning, boolean silent) {
    super(silent);
    this.warning = warning;
  }
}
