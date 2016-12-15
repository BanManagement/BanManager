package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpMuteData;

public class IpMuteEvent extends SilentCancellableEvent {

  @Getter
  private IpMuteData mute;

  public IpMuteEvent(IpMuteData mute, boolean silent) {
    super(silent);
    this.mute = mute;
  }
}
