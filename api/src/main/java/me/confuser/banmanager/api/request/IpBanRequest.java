package me.confuser.banmanager.api.request;

import inet.ipaddr.IPAddress;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing an IP ban to create.
 *
 * <p>The {@link String}-accepting constructor and {@link #ip(String)}
 * setter let callers avoid a direct compile-time dependency on
 * {@code com.github.seancfoley:ipaddress} for the common create-and-publish
 * flow. Reading {@link #ip()} still returns {@link IPAddress} — pre-event
 * handlers that mutate the request based on the parsed IP need the
 * dependency, but the request-building site does not.</p>
 */
public final class IpBanRequest {

  private IPAddress ip;
  private UUID actor;
  private String reason = "";
  private long expires;
  private boolean silent;

  public IpBanRequest() {}

  public IpBanRequest(IPAddress ip, UUID actor, String reason) {
    this.ip = Objects.requireNonNull(ip, "ip");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
  }

  /**
   * Convenience constructor that parses {@code ip} via the bundled
   * {@code com.github.seancfoley:ipaddress} library so callers don't have
   * to import it directly.
   *
   * @throws IllegalArgumentException when {@code ip} is null, blank, or not
   *                                  a valid IPv4 / IPv6 literal
   */
  public IpBanRequest(String ip, UUID actor, String reason) {
    this(IpAddresses.parse(ip), actor, reason);
  }

  public IPAddress ip() { return ip; }
  public IpBanRequest ip(IPAddress ip) { this.ip = ip; return this; }

  /**
   * Set the IP from its textual form. See {@link #IpBanRequest(String, UUID, String)}.
   */
  public IpBanRequest ip(String ip) { return ip(IpAddresses.parse(ip)); }

  public UUID actor() { return actor; }
  public IpBanRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public IpBanRequest reason(String reason) { this.reason = reason; return this; }

  public long expires() { return expires; }
  public IpBanRequest expires(long expires) { this.expires = expires; return this; }

  public boolean silent() { return silent; }
  public IpBanRequest silent(boolean silent) { this.silent = silent; return this; }
}
