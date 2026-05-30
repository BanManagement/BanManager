package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

/**
 * Post-event fired after a player has been kicked from the server via
 * BanManager's kick handling.
 */
public final class PlayerKickedEvent implements BanManagerEvent {

  private final int id;
  private final Player player;
  private final Player actor;
  private final String reason;
  private final long created;
  private final boolean silent;

  public PlayerKickedEvent(int id, Player player, Player actor, String reason, long created, boolean silent) {
    this.id = id;
    this.player = Objects.requireNonNull(player, "player");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.created = created;
    this.silent = silent;
  }

  /**
   * Storage row id of the persisted kick record.
   */
  public int id() { return id; }
  public Player player() { return player; }
  public Player actor() { return actor; }
  public String reason() { return reason; }
  /**
   * Unix timestamp seconds the kick was recorded.
   */
  public long created() { return created; }
  public boolean silent() { return silent; }
}
