package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerMuteData;


public class PlayerMutedEvent extends SilentEvent {

  @Getter
  private PlayerMuteData mute;

  public PlayerMutedEvent(PlayerMuteData mute, boolean silent) {
    super(silent, true);
    this.mute = mute;
  }
}
