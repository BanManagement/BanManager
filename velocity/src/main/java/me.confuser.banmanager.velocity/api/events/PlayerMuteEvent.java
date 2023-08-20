package me.confuser.banmanager.velocity.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerMuteData;


public class PlayerMuteEvent extends SilentCancellableEvent {

  @Getter
  private PlayerMuteData mute;

  public PlayerMuteEvent(PlayerMuteData mute, boolean silent) {
    super(silent);
    this.mute = mute;
  }
}
