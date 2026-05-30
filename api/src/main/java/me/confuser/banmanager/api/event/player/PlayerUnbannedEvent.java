package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

/**
 * Post-event fired after a player ban has been removed.
 */
public final class PlayerUnbannedEvent implements BanManagerEvent {

  private final PlayerBan ban;
  private final Player actor;
  private final String reason;
  private final boolean silent;

  public PlayerUnbannedEvent(PlayerBan ban, Player actor, String reason, boolean silent) {
    this.ban = Objects.requireNonNull(ban, "ban");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public PlayerBan ban() { return ban; }
  public Player actor() { return actor; }
  public String reason() { return reason; }
  public boolean silent() { return silent; }
}
