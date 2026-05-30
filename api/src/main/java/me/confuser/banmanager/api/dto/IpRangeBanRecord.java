package me.confuser.banmanager.api.dto;

import inet.ipaddr.IPAddress;

import java.util.Objects;

/**
 * Historical IP range ban record.
 */
public record IpRangeBanRecord(
    int id,
    IPAddress fromIp,
    IPAddress toIp,
    Player actor,
    Player pastActor,
    String reason,
    String createdReason,
    long expired,
    long pastCreated,
    long created,
    boolean silent
) {

  public IpRangeBanRecord {
    Objects.requireNonNull(fromIp, "fromIp");
    Objects.requireNonNull(toIp, "toIp");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(pastActor, "pastActor");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(createdReason, "createdReason");
  }
}
