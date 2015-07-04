package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerMutedEvent extends SilentEvent {

  @Getter
  private PlayerMuteData mute;

  public PlayerMutedEvent(PlayerMuteData mute, boolean silent) {
    super(silent);
    this.mute = mute;
  }
}
