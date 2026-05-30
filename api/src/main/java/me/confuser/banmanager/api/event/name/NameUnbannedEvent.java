package me.confuser.banmanager.api.event.name;

import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class NameUnbannedEvent implements BanManagerEvent {

  private final NameBan ban;
  private final Player actor;
  private final String reason;
  private final boolean silent;

  public NameUnbannedEvent(NameBan ban, Player actor, String reason, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public NameBan ban() { return ban; }
  public Player actor() { return actor; }
  public String reason() { return reason; }
  public boolean silent() { return silent; }
}
