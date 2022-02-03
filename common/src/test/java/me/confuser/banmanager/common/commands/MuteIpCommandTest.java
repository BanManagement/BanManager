package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.configs.ExemptionsConfig;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.util.IPUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MuteIpCommandTest extends BasePluginDbTest {
  private MuteIpCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("muteip")) {
        this.cmd = (MuteIpCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{faker.internet().ipV6Address()};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", faker.internet().ipV6Address(), "test"};

    when(sender.hasPermission(cmd.getPermission() + ".silent")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }

  @Test
  public void shouldFailIfSelf() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"Console", "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou cannot perform that action on yourself!");
  }

  @Test
  public void shouldFailIfAlreadyBanned() throws SQLException {
    IPAddress ip = IPUtils.toIPAddress(faker.internet().ipV6Address());
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{ip.toString(), "test"};

    assert plugin.getIpMuteStorage().mute(new IpMuteData(ip, sender.getData(), args[1], true, false));

    when(sender.hasPermission(cmd.getPermission() + ".override")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + ip.toString() + " is already muted");
  }

  @Test
  public void shouldFailIfExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName(), "test"};

    when(sender.hasPermission("bm.exempt.override.muteip")).thenReturn(false);
    when(commonPlayer.hasPermission("bm.exempt.muteip")).thenReturn(true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldFailIfNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{testUtils.createRandomPlayerName(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + args[0] + " not found, are you sure they exist?");
  }

  @Test
  public void shouldFailIfOfflineExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    ExemptionsConfig config = spy(plugin.getExemptionsConfig());
    String[] args = new String[]{player.getName(), "test"};

    when(sender.hasPermission("bm.exempt.override.muteip")).thenReturn(false);
    when(config.isExempt(player, "ban")).thenReturn(true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldMuteIp() {
    IPAddress ip = IPUtils.toIPAddress(faker.internet().ipV6Address());
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{ip.toString(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getIpMuteStorage().isMuted(ip));
    IpMuteData mute = plugin.getIpMuteStorage().getMute(ip);

    assertEquals(ip, mute.getIp());
    assertEquals("test", mute.getReason());
    assertEquals(sender.getName(), mute.getActor().getName());
    assertFalse(mute.isSilent());
  }

  @Test
  public void shouldMuteIpSilently() {
    IPAddress ip = IPUtils.toIPAddress(faker.internet().ipV6Address());
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{ip.toString(), "test", "-s"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getIpMuteStorage().isMuted(ip));

    assertTrue(plugin.getIpMuteStorage().getMute(ip).isSilent());
  }

  @Test
  public void shouldMuteIpSoftly() {
    IPAddress ip = IPUtils.toIPAddress(faker.internet().ipV6Address());
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{ip.toString(), "test", "-st"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getIpMuteStorage().isMuted(ip));
    IpMuteData mute = plugin.getIpMuteStorage().getMute(ip);

    assertEquals(ip, mute.getIp());
    assertEquals("test", mute.getReason());
    assertEquals(sender.getName(), mute.getActor().getName());
    assertFalse(mute.isSilent());
    assertTrue(mute.isSoft());
  }
}