package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.AbstractCancellableEvent;

import java.util.Objects;

/**
 * Pre-event fired before a player ban is removed. Cancellable.
 */
public final class PlayerUnbanEvent extends AbstractCancellableEvent {

  private final PlayerBan ban;
  private final Player actor;
  private String reason;
  private boolean silent;

  public PlayerUnbanEvent(PlayerBan ban, Player actor, String reason, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public PlayerBan ban() { return ban; }
  public Player actor() { return actor; }

  public String reason() { return reason; }
  public PlayerUnbanEvent reason(String reason) { this.reason = reason; return this; }

  public boolean silent() { return silent; }
  public PlayerUnbanEvent silent(boolean silent) { this.silent = silent; return this; }
}
