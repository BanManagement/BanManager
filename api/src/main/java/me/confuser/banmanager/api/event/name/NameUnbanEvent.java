package me.confuser.banmanager.api.event.name;

import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.AbstractCancellableEvent;

import java.util.Objects;

public final class NameUnbanEvent extends AbstractCancellableEvent {

  private final NameBan ban;
  private final Player actor;
  private String reason;
  private boolean silent;

  public NameUnbanEvent(NameBan ban, Player actor, String reason, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public NameBan ban() { return ban; }
  public Player actor() { return actor; }

  public String reason() { return reason; }
  public NameUnbanEvent reason(String reason) { this.reason = reason; return this; }

  public boolean silent() { return silent; }
  public NameUnbanEvent silent(boolean silent) { this.silent = silent; return this; }
}
