package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Historical name ban record.
 */
public record NameBanRecord(
    int id,
    String name,
    Player actor,
    Player pastActor,
    String reason,
    String createdReason,
    long expired,
    long pastCreated,
    long created,
    boolean silent
) {

  public NameBanRecord {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(pastActor, "pastActor");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(createdReason, "createdReason");
  }
}
