package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.AbstractCancellableEvent;

import java.util.Objects;

public final class IpRangeUnbanEvent extends AbstractCancellableEvent {

  private final IpRangeBan ban;
  private final Player actor;
  private String reason;
  private boolean silent;

  public IpRangeUnbanEvent(IpRangeBan ban, Player actor, String reason, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public IpRangeBan ban() { return ban; }
  public Player actor() { return actor; }

  public String reason() { return reason; }
  public IpRangeUnbanEvent reason(String reason) { this.reason = reason; return this; }

  public boolean silent() { return silent; }
  public IpRangeUnbanEvent silent(boolean silent) { this.silent = silent; return this; }
}
