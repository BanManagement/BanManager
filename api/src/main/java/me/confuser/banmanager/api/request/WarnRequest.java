package me.confuser.banmanager.api.request;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing a player warning to create.
 */
public final class WarnRequest {

  private UUID player;
  private UUID actor;
  private String reason = "";
  private double points = 1.0;
  private boolean read = true;
  private boolean silent;
  private long expires;

  public WarnRequest() {}

  public WarnRequest(UUID player, UUID actor, String reason) {
    this.player = Objects.requireNonNull(player, "player");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  public UUID player() { return player; }
  public WarnRequest player(UUID player) { this.player = player; return this; }

  public UUID actor() { return actor; }
  public WarnRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public WarnRequest reason(String reason) { this.reason = reason; return this; }

  public double points() { return points; }
  public WarnRequest points(double points) { this.points = points; return this; }

  public boolean read() { return read; }
  public WarnRequest read(boolean read) { this.read = read; return this; }

  public boolean silent() { return silent; }
  public WarnRequest silent(boolean silent) { this.silent = silent; return this; }

  public long expires() { return expires; }
  public WarnRequest expires(long expires) { this.expires = expires; return this; }
}
