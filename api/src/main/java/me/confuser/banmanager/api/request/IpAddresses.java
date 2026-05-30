package me.confuser.banmanager.api.request;

import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;

/**
 * Internal helper for the {@code String}-accepting IP request constructors.
 * Kept package-private so it is not part of the public API surface — callers
 * that want to parse an IP themselves should use
 * {@link inet.ipaddr.IPAddressString} directly.
 */
final class IpAddresses {

  private IpAddresses() {}

  /**
   * Parse a textual IPv4 / IPv6 address.
   *
   * @throws IllegalArgumentException when {@code text} is null, blank, or
   *                                  not a valid IPv4 / IPv6 literal
   */
  static IPAddress parse(String text) {
    if (text == null || text.isBlank()) {
      throw new IllegalArgumentException("ip must not be null or blank");
    }
    IPAddress address = new IPAddressString(text.trim()).getAddress();
    if (address == null) {
      throw new IllegalArgumentException("Not a valid IP address: " + text);
    }
    return address;
  }
}
