package me.confuser.banmanager.api.request;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing a player report to create.
 */
public final class ReportRequest {

  private UUID player;
  private UUID actor;
  private String reason = "";

  public ReportRequest() {}

  public ReportRequest(UUID player, UUID actor, String reason) {
    this.player = Objects.requireNonNull(player, "player");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  public UUID player() { return player; }
  public ReportRequest player(UUID player) { this.player = player; return this; }

  public UUID actor() { return actor; }
  public ReportRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public ReportRequest reason(String reason) { this.reason = reason; return this; }
}
