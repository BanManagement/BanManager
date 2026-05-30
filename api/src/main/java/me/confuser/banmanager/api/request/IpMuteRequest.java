package me.confuser.banmanager.api.request;

import inet.ipaddr.IPAddress;

import java.util.Objects;
import java.util.UUID;

/**
 * Mutable request describing an IP mute to create.
 *
 * <p>See {@link IpBanRequest} for the {@link String}-accepting overloads
 * pattern — same shape applies here.</p>
 */
public final class IpMuteRequest {

  private IPAddress ip;
  private UUID actor;
  private String reason = "";
  private long expires;
  private boolean soft;
  private boolean silent;

  public IpMuteRequest() {}

  public IpMuteRequest(IPAddress ip, UUID actor, String reason) {
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
  public IpMuteRequest(String ip, UUID actor, String reason) {
    this(IpAddresses.parse(ip), actor, reason);
  }

  public IPAddress ip() { return ip; }
  public IpMuteRequest ip(IPAddress ip) { this.ip = ip; return this; }

  /** Set the IP from its textual form. See {@link #IpMuteRequest(String, UUID, String)}. */
  public IpMuteRequest ip(String ip) { return ip(IpAddresses.parse(ip)); }

  public UUID actor() { return actor; }
  public IpMuteRequest actor(UUID actor) { this.actor = actor; return this; }

  public String reason() { return reason; }
  public IpMuteRequest reason(String reason) { this.reason = reason; return this; }

  public long expires() { return expires; }
  public IpMuteRequest expires(long expires) { this.expires = expires; return this; }

  public boolean soft() { return soft; }
  public IpMuteRequest soft(boolean soft) { this.soft = soft; return this; }

  public boolean silent() { return silent; }
  public IpMuteRequest silent(boolean silent) { this.silent = silent; return this; }
}
