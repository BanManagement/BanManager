package me.confuser.banmanager.api.request;

import inet.ipaddr.IPAddress;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing an IP range ban to create. The range is
 * inclusive on both ends.
 *
 * <p>See {@link IpBanRequest} for the {@link String}-accepting overloads
 * pattern — same shape applies here, with one overload per endpoint.</p>
 */
public final class IpRangeBanRequest {

  private IPAddress fromIp;
  private IPAddress toIp;
  private UUID actor;
  private String reason = "";
  private long expires;
  private boolean silent;

  public IpRangeBanRequest() {}

  public IpRangeBanRequest(IPAddress fromIp, IPAddress toIp, UUID actor, String reason) {
    this.fromIp = Objects.requireNonNull(fromIp, "fromIp");
    this.toIp = Objects.requireNonNull(toIp, "toIp");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  /**
   * Convenience constructor that parses {@code fromIp} / {@code toIp} via
   * the bundled {@code com.github.seancfoley:ipaddress} library so callers
   * don't have to import it directly.
   *
   * @throws IllegalArgumentException when either endpoint is null, blank,
   *                                  or not a valid IPv4 / IPv6 literal
   */
  public IpRangeBanRequest(String fromIp, String toIp, UUID actor, String reason) {
    this(IpAddresses.parse(fromIp), IpAddresses.parse(toIp), actor, reason);
  }

  public IPAddress fromIp() { return fromIp; }
  public IpRangeBanRequest fromIp(IPAddress fromIp) { this.fromIp = fromIp; return this; }

  /** Set the lower bound from its textual form. */
  public IpRangeBanRequest fromIp(String fromIp) { return fromIp(IpAddresses.parse(fromIp)); }

  public IPAddress toIp() { return toIp; }
  public IpRangeBanRequest toIp(IPAddress toIp) { this.toIp = toIp; return this; }

  /** Set the upper bound from its textual form. */
  public IpRangeBanRequest toIp(String toIp) { return toIp(IpAddresses.parse(toIp)); }

  public UUID actor() { return actor; }
  public IpRangeBanRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public IpRangeBanRequest reason(String reason) { this.reason = reason; return this; }

  public long expires() { return expires; }
  public IpRangeBanRequest expires(long expires) { this.expires = expires; return this; }

  public boolean silent() { return silent; }
  public IpRangeBanRequest silent(boolean silent) { this.silent = silent; return this; }
}
