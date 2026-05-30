package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.common.exception.BanManagerInternalException;
import me.confuser.banmanager.common.ipaddr.AddressStringException;
import me.confuser.banmanager.common.ipaddr.IPAddressString;

/**
 * Converts between BanManager's shaded {@code me.confuser.banmanager.common.ipaddr.IPAddress}
 * (used internally by ORMLite entities) and the unshaded {@code inet.ipaddr.IPAddress}
 * exposed on the public API. The two classes are bytecode-identical but live in
 * different packages, so direct casting is impossible.
 *
 * <p>Conversion is via canonical string form: cheap, allocation-light, and
 * round-trips for both IPv4 and IPv6 including subnets.</p>
 */
public final class IpAddressMapper {

  private IpAddressMapper() {}

  /**
   * Convert from internal (shaded) to public API (unshaded).
   */
  public static inet.ipaddr.IPAddress toApi(me.confuser.banmanager.common.ipaddr.IPAddress internal) {
    if (internal == null) return null;
    try {
      return new inet.ipaddr.IPAddressString(internal.toCanonicalString()).toAddress();
    } catch (inet.ipaddr.AddressStringException e) {
      throw new BanManagerInternalException("Failed to convert internal IPAddress '" + internal + "' to API form", e);
    }
  }

  /**
   * Convert from public API (unshaded) to internal (shaded).
   */
  public static me.confuser.banmanager.common.ipaddr.IPAddress toInternal(inet.ipaddr.IPAddress api) {
    if (api == null) return null;
    try {
      return new IPAddressString(api.toCanonicalString()).toAddress();
    } catch (AddressStringException e) {
      throw new BanManagerInternalException("Failed to convert API IPAddress '" + api + "' to internal form", e);
    }
  }
}
