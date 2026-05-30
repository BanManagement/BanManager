package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class IpUnmutedEvent implements BanManagerEvent {

  private final IpMute mute;
  private final Player actor;
  private final String reason;
  private final boolean silent;

  public IpUnmutedEvent(IpMute mute, Player actor, String reason, boolean silent) {
    this.mute = Objects.requireNonNull(mute, "mute");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public IpMute mute() { return mute; }
  public Player actor() { return actor; }
  public String reason() { return reason; }
  public boolean silent() { return silent; }
}
