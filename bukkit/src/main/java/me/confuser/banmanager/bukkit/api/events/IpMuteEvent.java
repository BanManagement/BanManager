package me.confuser.banmanager.bukkit.api.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpMuteData;

public class IpMuteEvent extends SilentCancellableEvent {

  @Getter
  private IpMuteData mute;

  public IpMuteEvent(IpMuteData mute, boolean silent) {
    super(silent, true);
    this.mute = mute;
  }
}
