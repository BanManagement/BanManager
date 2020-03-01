package me.confuser.banmanager.bungee.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;


public class PlayerUnmuteEvent extends CustomCancellableEvent {

  @Getter
  private PlayerMuteData mute;
  @Getter
  private PlayerData actor;
  @Getter
  private String reason;

  public PlayerUnmuteEvent(PlayerMuteData mute, PlayerData actor, String reason) {
    super();

    this.mute = mute;
    this.actor = actor;
    this.reason = reason;
  }
}
