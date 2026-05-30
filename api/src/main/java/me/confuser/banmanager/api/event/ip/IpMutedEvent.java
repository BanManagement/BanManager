package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class IpMutedEvent implements BanManagerEvent {

  private final IpMute mute;
  private final boolean silent;

  public IpMutedEvent(IpMute mute, boolean silent) {
    this.mute = Objects.requireNonNull(mute, "mute");
    this.silent = silent;
  }

  public IpMute mute() { return mute; }
  public boolean silent() { return silent; }
}
