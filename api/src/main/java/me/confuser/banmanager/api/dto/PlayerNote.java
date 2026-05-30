package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Internal note attached to a player.
 *
 * @param id storage row id
 * @param player the note subject
 * @param actor who wrote the note
 * @param message note body
 * @param created unix timestamp seconds the note was created
 */
public record PlayerNote(
    int id,
    Player player,
    Player actor,
    String message,
    long created
) {

  public PlayerNote {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(message, "message");
  }
}
