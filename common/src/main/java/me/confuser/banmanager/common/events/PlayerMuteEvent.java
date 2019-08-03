package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.PlayerMuteData;

public class PlayerMuteEvent extends CommonSilentCancellableEvent {

  @Getter
  private PlayerMuteData mute;

  public PlayerMuteEvent(PlayerMuteData mute, boolean silent) {
    super(silent, true);
    this.mute = mute;
  }
}
