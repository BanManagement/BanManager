package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerMuteData;


public class PlayerMutedEvent extends SilentEvent {

  @Getter
  private PlayerMuteData mute;

  public PlayerMutedEvent(PlayerMuteData mute, boolean silent) {
    super(silent);
    this.mute = mute;
  }
}
