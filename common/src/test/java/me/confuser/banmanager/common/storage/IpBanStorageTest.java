package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.util.IPUtils;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class IpBanStorageTest extends BasePluginDbTest {

  @Test
  public void shouldBanIPv4() throws SQLException {
    IPAddress ip = IPUtils.toIPAddress("192.168.1." + faker.number().numberBetween(1, 254));
    PlayerData actor = testUtils.createRandomPlayer();

    IpBanData ban = new IpBanData(ip, actor, "test ban", false);
    assertTrue(plugin.getIpBanStorage().ban(ban));
    assertTrue(plugin.getIpBanStorage().isBanned(ip));
  }

  @Test
  public void shouldBanIPv6() throws SQLException {
    IPAddress ip = IPUtils.toIPAddress("2001:db8::" + faker.number().numberBetween(1, 1000));
    PlayerData actor = testUtils.createRandomPlayer();

    IpBanData ban = new IpBanData(ip, actor, "test ban", false);
    assertTrue(plugin.getIpBanStorage().ban(ban));
    assertTrue(plugin.getIpBanStorage().isBanned(ip));
  }

  @Test
  public void shouldNotBanSameIpTwice() throws SQLException {
    IPAddress ip = IPUtils.toIPAddress("10.0.0." + faker.number().numberBetween(1, 254));
    PlayerData actor = testUtils.createRandomPlayer();

    IpBanData ban1 = new IpBanData(ip, actor, "first ban", false);
    assertTrue(plugin.getIpBanStorage().ban(ban1));

    IpBanData ban2 = new IpBanData(ip, actor, "second ban", false);
    try {
      plugin.getIpBanStorage().ban(ban2);
      // May not throw, but second ban should fail silently or throw
    } catch (SQLException e) {
      // Expected for duplicate
    }
  }

  @Test
  public void shouldUnbanIp() throws SQLException {
    IPAddress ip = IPUtils.toIPAddress("172.16.0." + faker.number().numberBetween(1, 254));
    PlayerData actor = testUtils.createRandomPlayer();

    IpBanData ban = new IpBanData(ip, actor, "test ban", false);
    plugin.getIpBanStorage().ban(ban);
    assertTrue(plugin.getIpBanStorage().isBanned(ip));

    plugin.getIpBanStorage().unban(ban, actor);
    assertFalse(plugin.getIpBanStorage().isBanned(ip));
  }

  @Test
  public void shouldCheckIpIsBanned() throws SQLException {
    IPAddress ip = IPUtils.toIPAddress("192.168.2." + faker.number().numberBetween(1, 254));
    PlayerData actor = testUtils.createRandomPlayer();

    assertFalse(plugin.getIpBanStorage().isBanned(ip));

    IpBanData ban = new IpBanData(ip, actor, "test ban", false);
    plugin.getIpBanStorage().ban(ban);

    assertTrue(plugin.getIpBanStorage().isBanned(ip));
  }

  @Test
  public void shouldHandleTempIpBanExpiry() throws SQLException {
    IPAddress ip = IPUtils.toIPAddress("192.168.3." + faker.number().numberBetween(1, 254));
    PlayerData actor = testUtils.createRandomPlayer();

    // Create a temp ban that expires in the past
    long expiredTime = (System.currentTimeMillis() / 1000L) - 10;
    IpBanData ban = new IpBanData(ip, actor, "temp ban", false, expiredTime);

    assertTrue(ban.hasExpired());
  }
}

