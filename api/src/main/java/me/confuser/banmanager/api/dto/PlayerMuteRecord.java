package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Historical mute record.
 */
public record PlayerMuteRecord(
    int id,
    Player player,
    Player actor,
    Player pastActor,
    String reason,
    String createdReason,
    long expired,
    long pastCreated,
    long created,
    boolean soft,
    boolean silent,
    boolean onlineOnly,
    long remainingOnlineTime
) {

  public PlayerMuteRecord {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(pastActor, "pastActor");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(createdReason, "createdReason");
  }
}
