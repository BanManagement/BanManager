package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class IpRangeUnbannedEvent implements BanManagerEvent {

  private final IpRangeBan ban;
  private final Player actor;
  private final String reason;
  private final boolean silent;

  public IpRangeUnbannedEvent(IpRangeBan ban, Player actor, String reason, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public IpRangeBan ban() { return ban; }
  public Player actor() { return actor; }
  public String reason() { return reason; }
  public boolean silent() { return silent; }
}
