package me.confuser.banmanager.api.request;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing a player note to create.
 */
public final class NoteRequest {

  private UUID player;
  private UUID actor;
  private String message = "";

  public NoteRequest() {}

  public NoteRequest(UUID player, UUID actor, String message) {
    this.player = Objects.requireNonNull(player, "player");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.message = Objects.requireNonNull(message, "message");
  }

  public UUID player() { return player; }
  public NoteRequest player(UUID player) { this.player = player; return this; }

  public UUID actor() { return actor; }
  public NoteRequest actor(UUID actor) { this.actor = actor; return this; }

  public String message() { return message; }
  public NoteRequest message(String message) { this.message = message; return this; }
}
