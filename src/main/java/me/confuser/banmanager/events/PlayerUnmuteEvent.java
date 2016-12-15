package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;

public class PlayerUnmuteEvent extends CustomCancellableEvent {

  @Getter
  private PlayerMuteData mute;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public PlayerUnmuteEvent(PlayerMuteData mute, PlayerData actor, String reason) {
    this.mute = mute;
    this.actor = actor;
    this.reason = reason;
  }
}
