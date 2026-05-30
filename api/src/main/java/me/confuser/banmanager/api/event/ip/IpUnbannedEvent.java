package me.confuser.banmanager.api.event.ip;

import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class IpUnbannedEvent implements BanManagerEvent {

  private final IpBan ban;
  private final Player actor;
  private final String reason;
  private final boolean silent;

  public IpUnbannedEvent(IpBan ban, Player actor, String reason, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public IpBan ban() { return ban; }
  public Player actor() { return actor; }
  public String reason() { return reason; }
  public boolean silent() { return silent; }
}
