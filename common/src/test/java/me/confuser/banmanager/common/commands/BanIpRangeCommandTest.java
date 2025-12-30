package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressSeqRange;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
import me.confuser.banmanager.common.util.IPUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

public class BanIpRangeCommandTest extends BasePluginDbTest {
  private BanIpRangeCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("baniprange")) {
        this.cmd = (BanIpRangeCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{faker.internet().ipV6Cidr()};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", faker.internet().ipV6Cidr(), "test"};

    when(sender.hasPermission(cmd.getPermission() + ".silent")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }

  @Test
  public void shouldFailIfAlreadyBanned() throws SQLException {
    String cidr = faker.internet().ipV6Cidr();
    IPAddressString ipString = new IPAddressString(cidr);
    IPAddressSeqRange range = ipString.getSequentialRange();
    IPAddress fromIp = range.getLower();
    IPAddress toIp = range.getUpper();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{cidr, "test"};

    assert plugin.getIpRangeBanStorage().ban(new IpRangeBanData(fromIp, toIp, sender.getData(), args[1], true));

    when(sender.hasPermission(cmd.getPermission() + ".override")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cA ban containing those ranges already exists");
  }

  @Test
  public void shouldFailIfInvalidIp() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{testUtils.createRandomPlayerName(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cInvalid range, please use cidr notation 192.168.0.1/16 or wildcard 192.168.*.*");
  }

  @Test
  public void shouldBanRange() {
    String cidr = faker.internet().ipV6Cidr();
    IPAddressString ipString = new IPAddressString(cidr);
    IPAddressSeqRange range = ipString.getSequentialRange();
    IPAddress expectedFromIp = range.getLower();
    IPAddress expectedToIp = range.getUpper();

    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{cidr, "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getIpRangeBanStorage().isBanned(expectedToIp));
    IpRangeBanData ban = plugin.getIpRangeBanStorage().getBan(expectedToIp);

    // Compare byte arrays to avoid issues with IPAddress.equals() when objects are created differently
    assertArrayEquals(expectedFromIp.getBytes(), ban.getFromIp().getBytes());
    assertArrayEquals(expectedToIp.getBytes(), ban.getToIp().getBytes());
    assertEquals("test", ban.getReason());
    assertEquals(sender.getName(), ban.getActor().getName());
    assertFalse(ban.isSilent());
  }
}
