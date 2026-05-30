package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Warning issued to a player.
 *
 * @param id storage row id
 * @param player the warned player
 * @param actor who issued the warning
 * @param reason warning reason
 * @param points warning point value (defaults to 1.0)
 * @param read whether the warned player has acknowledged the warning
 * @param created unix timestamp seconds the warning was created
 * @param expires unix timestamp seconds the warning expires; {@code 0} means
 *                permanent
 */
public record PlayerWarn(
    int id,
    Player player,
    Player actor,
    String reason,
    double points,
    boolean read,
    long created,
    long expires
) {

  public PlayerWarn {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");
  }
}
