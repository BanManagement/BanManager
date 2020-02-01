package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerKickData;

public class PlayerKickedEvent extends SilentEvent {

  @Getter
  private PlayerKickData kick;

  public PlayerKickedEvent(PlayerKickData kick, boolean isSilent) {
    super(isSilent, true);
    this.kick = kick;
  }
}
