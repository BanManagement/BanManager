package me.confuser.banmanager.api.dto;

import inet.ipaddr.IPAddress;

import java.util.Objects;

/**
 * Active ban on a single IP address.
 *
 * @param id storage row id
 * @param ip the banned IP address
 * @param actor who issued the ban
 * @param reason ban reason
 * @param created unix timestamp seconds the ban was created
 * @param updated unix timestamp seconds the ban was last updated
 * @param expires unix timestamp seconds the ban expires; {@code 0} means
 *                permanent
 * @param silent whether the ban is silent (no broadcast)
 */
public record IpBan(
    int id,
    IPAddress ip,
    Player actor,
    String reason,
    long created,
    long updated,
    long expires,
    boolean silent
) {

  public IpBan {
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
