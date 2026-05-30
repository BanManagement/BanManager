package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Historical ban record. Created when an active {@link PlayerBan} is
 * unbanned without {@code delete=true}.
 *
 * @param id storage row id
 * @param player who was banned
 * @param actor who unbanned the player
 * @param pastActor who originally issued the ban
 * @param reason the original ban reason
 * @param createdReason the unban reason (may be empty)
 * @param expired the {@code expires} value of the original ban
 * @param pastCreated when the original ban was created
 * @param created when the unban happened
 * @param silent whether the original ban was silent
 */
public record PlayerBanRecord(
    int id,
    Player player,
    Player actor,
    Player pastActor,
    String reason,
    String createdReason,
    long expired,
    long pastCreated,
    long created,
    boolean silent
) {

  public PlayerBanRecord {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(pastActor, "pastActor");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(createdReason, "createdReason");
  }
}
