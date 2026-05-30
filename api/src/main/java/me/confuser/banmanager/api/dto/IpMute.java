package me.confuser.banmanager.api.dto;

import inet.ipaddr.IPAddress;

import java.util.Objects;

/**
 * Active mute on a single IP address.
 */
public record IpMute(
    int id,
    IPAddress ip,
    Player actor,
    String reason,
    long created,
    long updated,
    long expires,
    boolean soft,
    boolean silent
) {

  public IpMute {
    Objects.requireNonNull(ip, "ip");
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
