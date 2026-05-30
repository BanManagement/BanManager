package me.confuser.banmanager.api.request;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing a player ban to create. Pre-event handlers may
 * mutate any field on this object before persistence.
 *
 * <p>Time fields are unix timestamps in seconds. {@code expires == 0} means
 * the ban is permanent.</p>
 *
 * <p>Use the fluent setters for chaining:</p>
 *
 * <pre>{@code
 * service.bans().ban(new BanRequest()
 *     .player(playerUuid)
 *     .actor(actorUuid)
 *     .reason("griefing")
 *     .expires(System.currentTimeMillis() / 1000L + 3600));
 * }</pre>
 */
public final class BanRequest {

  private UUID player;
  private UUID actor;
  private String reason = "";
  private long expires;
  private boolean silent;

  public BanRequest() {}

  public BanRequest(UUID player, UUID actor, String reason) {
    this.player = Objects.requireNonNull(player, "player");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  public UUID player() { return player; }
  public BanRequest player(UUID player) { this.player = player; return this; }

  public UUID actor() { return actor; }
  public BanRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public BanRequest reason(String reason) { this.reason = reason; return this; }

  public long expires() { return expires; }
  public BanRequest expires(long expires) { this.expires = expires; return this; }

  public boolean silent() { return silent; }
  public BanRequest silent(boolean silent) { this.silent = silent; return this; }
}
