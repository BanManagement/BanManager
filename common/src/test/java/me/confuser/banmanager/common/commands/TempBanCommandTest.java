package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.configs.ExemptionsConfig;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TempBanCommandTest extends BasePluginDbTest {
  private TempBanCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("tempban")) {
        this.cmd = (TempBanCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser", "1d"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", "confuser", "1d", "test"};

    when(sender.hasPermission(cmd.getPermission() + ".silent")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }

  @Test
  public void shouldFailIfSelf() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"Console", "1d", "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&cYou cannot perform that action on yourself!");
  }

  @Test
  public void shouldFailIfAlreadyBanned() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{player.getName(), "1d", "test"};

    assert plugin.getPlayerBanStorage().ban(new PlayerBanData(player, sender.getData(), args[1], true));

    when(sender.hasPermission(cmd.getPermission() + ".override")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&c" + player.getName() + " is already banned");
  }

  @Test
  public void shouldFailIfOffline() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{testUtils.createRandomPlayerName(), "1d", "test"};

    when(sender.hasPermission(cmd.getPermission() + ".offline")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&cYou are not allowed to perform this action on an offline player");
  }

  @Test
  public void shouldFailIfExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName(), "1d", "test"};

    when(sender.hasPermission("bm.exempt.override.tempban")).thenReturn(false);
    when(commonPlayer.hasPermission("bm.exempt.ban")).thenReturn(true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldFailIfNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{faker.internet().uuid(), "1d", "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&c" + args[0] + " not found, are you sure they exist?");
  }

  @Test
  public void shouldFailIfOfflineExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    ExemptionsConfig config = spy(plugin.getExemptionsConfig());
    String[] args = new String[]{player.getName(), "1d", "test"};

    when(sender.hasPermission("bm.exempt.override.tempban")).thenReturn(false);
    when(config.isExempt(player, "tempban")).thenReturn(true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldBanPlayer() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "30y", "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerBanStorage().isBanned(player.getUUID()));
    PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

    assertEquals(player.getName(), ban.getPlayer().getName());
    assertEquals("test", ban.getReason());
    assertEquals(sender.getName(), ban.getActor().getName());
    assertFalse(ban.isSilent());
  }

  @Test
  public void shouldBanPlayerSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "30y", "test", "-s"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    assertTrue(plugin.getPlayerBanStorage().getBan(player.getUUID()).isSilent());
  }
}
