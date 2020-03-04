package me.confuser.banmanager.common.util;

import com.github.javafaker.Faker;
import inet.ipaddr.AddressStringException;
import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IPUtilsTest {
  private Faker faker = new Faker();

  @Test
  public void stringToBytes() {
  }

  @Test
  public void bytesToString() {

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
  public void inetToIPAddress() {

  }

  @Test
  public void bytesToIPAddress() {

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
  }
}
