package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerMuteEvent extends SilentCancellableEvent {

  @Getter
  private PlayerMuteData mute;

  public PlayerMuteEvent(PlayerMuteData mute, boolean silent) {
    super(silent);
    this.mute = mute;
  }
}
