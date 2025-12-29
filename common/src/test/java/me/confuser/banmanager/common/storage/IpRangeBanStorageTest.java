package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.util.IPUtils;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class IpRangeBanStorageTest extends BasePluginDbTest {

  @Test
  public void shouldBanIpRange() throws SQLException {
    IPAddress fromIp = IPUtils.toIPAddress("10.10.0.0");
    IPAddress toIp = IPUtils.toIPAddress("10.10.0.255");
    PlayerData actor = testUtils.createRandomPlayer();

    IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, "test range ban", false);
    assertTrue(plugin.getIpRangeBanStorage().ban(ban));
  }

  @Test
  public void shouldDetectIpInRange() throws SQLException {
    IPAddress fromIp = IPUtils.toIPAddress("10.20.0.0");
    IPAddress toIp = IPUtils.toIPAddress("10.20.0.255");
    PlayerData actor = testUtils.createRandomPlayer();

    IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, "test range ban", false);
    plugin.getIpRangeBanStorage().ban(ban);

    IPAddress withinRange = IPUtils.toIPAddress("10.20.0.100");
    assertTrue(plugin.getIpRangeBanStorage().isBanned(withinRange));

    IPAddress outsideRange = IPUtils.toIPAddress("10.21.0.100");
    assertFalse(plugin.getIpRangeBanStorage().isBanned(outsideRange));
  }

  @Test
  public void shouldHandleCIDRNotation() throws SQLException {
    // CIDR 10.30.0.0/24 = 10.30.0.0 to 10.30.0.255
    IPAddress fromIp = IPUtils.toIPAddress("10.30.0.0");
    IPAddress toIp = IPUtils.toIPAddress("10.30.0.255");
    PlayerData actor = testUtils.createRandomPlayer();

    IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, "CIDR ban", false);
    assertTrue(plugin.getIpRangeBanStorage().ban(ban));

    assertTrue(plugin.getIpRangeBanStorage().isBanned(IPUtils.toIPAddress("10.30.0.1")));
    assertTrue(plugin.getIpRangeBanStorage().isBanned(IPUtils.toIPAddress("10.30.0.254")));
  }

  @Test
  public void shouldUnbanIpRange() throws SQLException {
    IPAddress fromIp = IPUtils.toIPAddress("10.40.0.0");
    IPAddress toIp = IPUtils.toIPAddress("10.40.0.255");
    PlayerData actor = testUtils.createRandomPlayer();

    IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, "test range ban", false);
    plugin.getIpRangeBanStorage().ban(ban);

    IPAddress withinRange = IPUtils.toIPAddress("10.40.0.50");
    assertTrue(plugin.getIpRangeBanStorage().isBanned(withinRange));

    plugin.getIpRangeBanStorage().unban(ban, actor);
    assertFalse(plugin.getIpRangeBanStorage().isBanned(withinRange));
  }

  @Test
  public void shouldHandleTempRangeBanExpiry() throws SQLException {
    IPAddress fromIp = IPUtils.toIPAddress("10.50.0.0");
    IPAddress toIp = IPUtils.toIPAddress("10.50.0.255");
    PlayerData actor = testUtils.createRandomPlayer();

    // Create a temp range ban that expires in the past
    long expiredTime = (System.currentTimeMillis() / 1000L) - 10;
    IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, "temp range ban", false, expiredTime);

    assertTrue(ban.hasExpired());
  }
}

