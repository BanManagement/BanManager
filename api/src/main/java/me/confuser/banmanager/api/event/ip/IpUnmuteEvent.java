package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.AbstractCancellableEvent;

import java.util.Objects;

public final class IpUnmuteEvent extends AbstractCancellableEvent {

  private final IpMute mute;
  private final Player actor;
  private String reason;
  private boolean silent;

  public IpUnmuteEvent(IpMute mute, Player actor, String reason, boolean silent) {
    this.mute = Objects.requireNonNull(mute, "mute");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public IpMute mute() { return mute; }
  public Player actor() { return actor; }

  public String reason() { return reason; }
  public IpUnmuteEvent reason(String reason) { this.reason = reason; return this; }

  public boolean silent() { return silent; }
  public IpUnmuteEvent silent(boolean silent) { this.silent = silent; return this; }
}
