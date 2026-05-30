package me.confuser.banmanager.api.dto;

import inet.ipaddr.IPAddress;

import java.util.Objects;

/**
 * Historical IP mute record.
 */
public record IpMuteRecord(
    int id,
    IPAddress ip,
    Player actor,
    Player pastActor,
    String reason,
    String createdReason,
    long expired,
    long pastCreated,
    long created,
    boolean soft,
    boolean silent
) {

  public IpMuteRecord {
    Objects.requireNonNull(ip, "ip");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(pastActor, "pastActor");
    Objects.requireNonNull(reason, "reason");
    Objects.requireNonNull(createdReason, "createdReason");
  }
}
