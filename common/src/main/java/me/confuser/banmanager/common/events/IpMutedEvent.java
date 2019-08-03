package me.confuser.banmanager.common.events;

import lombok.Getter;
import me.confuser.banmanager.common.data.IpMuteData;

public class IpMutedEvent extends CommonSilentEvent {

  @Getter
  private IpMuteData mute;

  public IpMutedEvent(IpMuteData mute, boolean silent) {
    super(silent, true);
    this.mute = mute;
  }
}
