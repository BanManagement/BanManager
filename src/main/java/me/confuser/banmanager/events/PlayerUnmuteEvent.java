package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerUnmuteEvent extends CustomCancellableEvent {

  @Getter
  private PlayerMuteData mute;
  @Getter
  private String reason;

  public PlayerUnmuteEvent(PlayerMuteData mute, String reason) {
    this.mute = mute;
    this.reason = reason;
  }
}
