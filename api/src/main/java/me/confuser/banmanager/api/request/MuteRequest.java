package me.confuser.banmanager.api.request;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing a player mute to create.
 */
public final class MuteRequest {

  private UUID player;
  private UUID actor;
  private String reason = "";
  private long expires;
  private boolean soft;
  private boolean silent;
  private boolean onlineOnly;

  public MuteRequest() {}

  public MuteRequest(UUID player, UUID actor, String reason) {
    this.player = Objects.requireNonNull(player, "player");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  public UUID player() { return player; }
  public MuteRequest player(UUID player) { this.player = player; return this; }

  public UUID actor() { return actor; }
  public MuteRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public MuteRequest reason(String reason) { this.reason = reason; return this; }

  public long expires() { return expires; }
  public MuteRequest expires(long expires) { this.expires = expires; return this; }

  public boolean soft() { return soft; }
  public MuteRequest soft(boolean soft) { this.soft = soft; return this; }

  public boolean silent() { return silent; }
  public MuteRequest silent(boolean silent) { this.silent = silent; return this; }

  public boolean onlineOnly() { return onlineOnly; }
  public MuteRequest onlineOnly(boolean onlineOnly) { this.onlineOnly = onlineOnly; return this; }
}
