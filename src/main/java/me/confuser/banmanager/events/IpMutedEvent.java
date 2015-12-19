package me.confuser.banmanager.events;

import lombok.Getter;
import me.confuser.banmanager.data.IpMuteData;

public class IpMutedEvent extends SilentEvent {

  @Getter
  private IpMuteData mute;

  public IpMutedEvent(IpMuteData mute, boolean silent) {
    super(silent);
    this.mute = mute;
  }
}
