package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.common.ipaddr.IPAddressString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Round-trip tests for {@link IpAddressMapper}. The two {@code IPAddress}
 * classes (shaded under {@code me.confuser.banmanager.common.ipaddr} and the
 * unshaded API form under {@code inet.ipaddr}) are bytecode-identical but
 * incompatible at the type level — we verify the mapper preserves canonical
 * form across both directions for IPv4, IPv6, and subnet inputs.
 */
public class IpAddressMapperTest {

  @Test
  public void nullInternalReturnsNullApi() {
    assertNull(IpAddressMapper.toApi(null));
  }

  @Test
  public void nullApiReturnsNullInternal() {
    assertNull(IpAddressMapper.toInternal(null));
  }

  @Test
  public void ipv4InternalRoundTripsToApi() throws Exception {
    me.confuser.banmanager.common.ipaddr.IPAddress internal =
        new IPAddressString("203.0.113.42").toAddress();

    inet.ipaddr.IPAddress api = IpAddressMapper.toApi(internal);

    assertNotNull(api);
    assertEquals("203.0.113.42", api.toCanonicalString());
  }

  @Test
  public void ipv4ApiRoundTripsToInternal() throws Exception {
    inet.ipaddr.IPAddress api =
        new inet.ipaddr.IPAddressString("198.51.100.7").toAddress();

    me.confuser.banmanager.common.ipaddr.IPAddress internal = IpAddressMapper.toInternal(api);

    assertNotNull(internal);
    assertEquals("198.51.100.7", internal.toCanonicalString());
  }

  @Test
  public void ipv6CompressedFormIsPreserved() throws Exception {
    me.confuser.banmanager.common.ipaddr.IPAddress internal =
        new IPAddressString("2001:db8::1").toAddress();

    inet.ipaddr.IPAddress api = IpAddressMapper.toApi(internal);

    assertNotNull(api);
    assertEquals(internal.toCanonicalString(), api.toCanonicalString());
  }

  @Test
  public void ipv6FullFormIsPreserved() throws Exception {
    me.confuser.banmanager.common.ipaddr.IPAddress internal =
        new IPAddressString("2001:0db8:85a3:0000:0000:8a2e:0370:7334").toAddress();

    inet.ipaddr.IPAddress api = IpAddressMapper.toApi(internal);

    assertNotNull(api);
    assertEquals(internal.toCanonicalString(), api.toCanonicalString());
  }

  @Test
  public void ipv4SubnetPrefixRoundTrips() throws Exception {
    me.confuser.banmanager.common.ipaddr.IPAddress internal =
        new IPAddressString("10.0.0.0/8").toAddress();

    inet.ipaddr.IPAddress api = IpAddressMapper.toApi(internal);

    assertNotNull(api);
    assertEquals(internal.toCanonicalString(), api.toCanonicalString());
  }

  @Test
  public void ipv6SubnetPrefixRoundTrips() throws Exception {
    me.confuser.banmanager.common.ipaddr.IPAddress internal =
        new IPAddressString("2001:db8::/48").toAddress();

    inet.ipaddr.IPAddress api = IpAddressMapper.toApi(internal);

    assertNotNull(api);
    assertEquals(internal.toCanonicalString(), api.toCanonicalString());
  }

  @Test
  public void doubleRoundTripPreservesAddress() throws Exception {
    me.confuser.banmanager.common.ipaddr.IPAddress original =
        new IPAddressString("203.0.113.42").toAddress();

    me.confuser.banmanager.common.ipaddr.IPAddress restored =
        IpAddressMapper.toInternal(IpAddressMapper.toApi(original));

    assertNotNull(restored);
    assertEquals(original.toCanonicalString(), restored.toCanonicalString());
  }

  @Test
  public void doubleRoundTripPreservesIpv6() throws Exception {
    me.confuser.banmanager.common.ipaddr.IPAddress original =
        new IPAddressString("2001:db8::dead:beef").toAddress();

    me.confuser.banmanager.common.ipaddr.IPAddress restored =
        IpAddressMapper.toInternal(IpAddressMapper.toApi(original));

    assertNotNull(restored);
    assertEquals(original.toCanonicalString(), restored.toCanonicalString());
  }
}
