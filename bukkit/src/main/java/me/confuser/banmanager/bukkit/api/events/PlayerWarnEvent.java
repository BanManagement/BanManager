package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerWarnData;


public class PlayerWarnEvent extends SilentEvent {

  @Getter
  private PlayerWarnData warning;

  public PlayerWarnEvent(PlayerWarnData warning, boolean silent) {
    super(silent, true);
    this.warning = warning;
  }
}
