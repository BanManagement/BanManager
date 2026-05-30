package me.confuser.banmanager.api.dto;

import inet.ipaddr.IPAddress;

import java.util.Objects;

/**
 * Active ban on a contiguous IP range (inclusive on both ends).
 */
public record IpRangeBan(
    int id,
    IPAddress fromIp,
    IPAddress toIp,
    Player actor,
    String reason,
    long created,
    long updated,
    long expires,
    boolean silent
) {

  public IpRangeBan {
    Objects.requireNonNull(fromIp, "fromIp");
    Objects.requireNonNull(toIp, "toIp");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");
  }

  public boolean isPermanent() {
    return expires == 0;
  }

  public boolean hasExpired() {
    return expires != 0 && expires <= (System.currentTimeMillis() / 1000L);
  }
}
