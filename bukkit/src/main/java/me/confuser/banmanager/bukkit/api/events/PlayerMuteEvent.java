package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerMuteData;


public class PlayerMuteEvent extends SilentCancellableEvent {

  @Getter
  private PlayerMuteData mute;

  public PlayerMuteEvent(PlayerMuteData mute, boolean silent) {
    super(silent, true);
    this.mute = mute;
  }
}
