package me.confuser.banmanager.common.util;

import com.github.javafaker.Faker;
import me.confuser.banmanager.common.ipaddr.AddressStringException;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;

public class IPUtilsTest {
  private Faker faker = new Faker();

  @Test
  public void stringToBytes() {
    // Test IPv4
    byte[] ipv4Bytes = IPUtils.toIPAddress("127.0.0.1").getBytes();
    assertEquals(4, ipv4Bytes.length);
    assertEquals(127, ipv4Bytes[0] & 0xFF);
    assertEquals(0, ipv4Bytes[1] & 0xFF);
    assertEquals(0, ipv4Bytes[2] & 0xFF);
    assertEquals(1, ipv4Bytes[3] & 0xFF);

    // Test IPv6 loopback
    byte[] ipv6Bytes = IPUtils.toIPAddress("::1").getBytes();
    assertEquals(16, ipv6Bytes.length);
    assertEquals(1, ipv6Bytes[15] & 0xFF);

    // Test random IPv4
    String ipv4 = faker.internet().ipV4Address();
    byte[] randomIpv4Bytes = IPUtils.toIPAddress(ipv4).getBytes();
    assertEquals(4, randomIpv4Bytes.length);
  }

  @Test
  public void bytesToString() {
    // Test IPv4 round trip
    IPAddress original = IPUtils.toIPAddress("192.168.1.100");
    byte[] bytes = original.getBytes();
    IPAddress reconstructed = IPUtils.toIPAddress(bytes);
    assertEquals(original.toString(), reconstructed.toString());

    // Test IPv6 round trip
    IPAddress originalIpv6 = IPUtils.toIPAddress("2001:db8::1");
    byte[] ipv6Bytes = originalIpv6.getBytes();
    IPAddress reconstructedIpv6 = IPUtils.toIPAddress(ipv6Bytes);
    assertNotNull(reconstructedIpv6);
  }

  @Test
  public void isInRange() throws AddressStringException {
    IPAddress from = IPUtils.toIPAddress("83.60.20.102");
    IPAddress to = IPUtils.toIPAddress("83.100.100.160");

    assertTrue(IPUtils.isInRange(from, to, IPUtils.toIPAddress("83.61.21.100")));

    IPAddress ipv4 = new IPAddressString(faker.internet().ipV4Cidr()).toAddress();
    IPAddress ipv6 = new IPAddressString(faker.internet().ipV6Cidr()).toAddress();

    assertTrue(IPUtils.isInRange(ipv4.getLower(), ipv4.getUpper(), ipv4.getIterable().iterator().next()));
    assertTrue(IPUtils.isInRange(ipv6.getLower(), ipv6.getUpper(), ipv6.getIterable().iterator().next()));
  }

  @Test
  public void inetToIPAddress() throws UnknownHostException {
    // Test IPv4 InetAddress
    InetAddress inet4 = InetAddress.getByName("192.168.1.1");
    IPAddress ip4 = IPUtils.toIPAddress(inet4);
    assertEquals("192.168.1.1", ip4.toString());

    // Test localhost
    InetAddress localhost = InetAddress.getByName("127.0.0.1");
    IPAddress localhostIp = IPUtils.toIPAddress(localhost);
    assertEquals("127.0.0.1", localhostIp.toString());

    // Test IPv6 InetAddress
    InetAddress inet6 = InetAddress.getByName("::1");
    IPAddress ip6 = IPUtils.toIPAddress(inet6);
    assertNotNull(ip6);
  }

  @Test
  public void bytesToIPAddress() {
    // Test IPv4 bytes
    byte[] ipv4Bytes = new byte[]{(byte) 192, (byte) 168, 1, 1};
    IPAddress ip4 = IPUtils.toIPAddress(ipv4Bytes);
    assertNotNull(ip4);
    assertEquals("192.168.1.1", ip4.toString());

    // Test IPv6 bytes (loopback)
    byte[] ipv6Bytes = new byte[16];
    ipv6Bytes[15] = 1;
    IPAddress ip6 = IPUtils.toIPAddress(ipv6Bytes);
    assertNotNull(ip6);
    assertEquals("::1", ip6.toString());

    // Test random IPv4
    String randomIpv4 = faker.internet().ipV4Address();
    IPAddress randomIp = IPUtils.toIPAddress(randomIpv4);
    byte[] randomBytes = randomIp.getBytes();
    IPAddress reconstructed = IPUtils.toIPAddress(randomBytes);
    assertEquals(randomIpv4, reconstructed.toString());
  }

  @Test
  public void stringToIPAddress() {
    assertEquals("127.0.0.1", IPUtils.toIPAddress("127.0.0.1").toString());
    assertEquals("::1", IPUtils.toIPAddress("::1").toString());

    String ipv4 = faker.internet().ipV4Address();
    String ipv6 = faker.internet().ipV6Address().replaceAll("0", "");

    assertEquals(ipv4, IPUtils.toIPAddress(ipv4).toString());
    assertEquals(ipv6, IPUtils.toIPAddress(ipv6).toString());
  }

  @Test
  public void isValid() {
    assertTrue(IPUtils.isValid("127.0.0.1"));
    assertTrue(IPUtils.isValid("::1"));

    String ipv4 = faker.internet().ipV4Address();
    String ipv6 = faker.internet().ipV6Address().replaceAll("0", "");

    assertTrue(IPUtils.isValid(ipv4));
    assertTrue(IPUtils.isValid(ipv6));

    // Test invalid IPs
    assertFalse(IPUtils.isValid("not-an-ip"));
    assertFalse(IPUtils.isValid("256.256.256.256"));
  }
}
