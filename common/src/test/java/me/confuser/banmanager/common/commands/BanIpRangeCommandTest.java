package me.confuser.banmanager.common.commands;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.util.IPUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
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
    IPAddress ip = IPUtils.toIPAddress(faker.internet().ipV6Cidr());
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{ip.toString(), "test"};

    assert plugin.getIpRangeBanStorage().ban(new IpRangeBanData(ip.getLower(), ip.getUpper(), sender.getData(), args[1], true));

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
    IPAddress ip = IPUtils.toIPAddress(faker.internet().ipV6Cidr());
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{ip.toString(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getIpRangeBanStorage().isBanned(ip.getUpper()));
    IpRangeBanData ban = plugin.getIpRangeBanStorage().getBan(ip.getUpper());

    assertEquals(ip.getLower(), ban.getFromIp());
    assertEquals(ip.getUpper(), ban.getToIp());
    assertEquals("test", ban.getReason());
    assertEquals(sender.getName(), ban.getActor().getName());
    assertFalse(ban.isSilent());
  }
}