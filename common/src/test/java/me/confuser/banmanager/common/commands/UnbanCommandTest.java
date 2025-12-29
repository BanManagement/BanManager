package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.parsers.UnbanCommandParser;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UnbanCommandTest extends BasePluginDbTest {
  private UnbanCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("unban")) {
        this.cmd = (UnbanCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNotBanned() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{player.getName()};

    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&c" + player.getName() + " is not banned");
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName};

    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&c" + playerName + " is not banned");
  }

  @Test
  public void shouldFailIfInvalidUUID() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String invalidUuid = "not-a-valid-uuid-format-here";
    String[] args = new String[]{invalidUuid};

    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&c" + invalidUuid + " not found, are you sure they exist?");
  }

  @Test
  public void shouldUnbanPlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // First ban the player
    PlayerBanData ban = testUtils.createBan(player, sender.getData(), "test ban");
    assertTrue(plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerBanStorage().isBanned(player.getUUID()));
    assertFalse(plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    // Verify record was created
    assertTrue(plugin.getPlayerBanRecordStorage().getCount(player) > 0);
  }

  @Test
  public void shouldUnbanByUUID() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    PlayerBanData ban = testUtils.createBan(player, sender.getData(), "test ban");
    assertTrue(plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    String[] args = new String[]{player.getUUID().toString()};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerBanStorage().isBanned(player.getUUID()));
    assertFalse(plugin.getPlayerBanStorage().isBanned(player.getUUID()));
  }

  @Test
  public void shouldUnbanWithDeleteFlag() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    when(sender.hasPermission(cmd.getPermission() + ".delete")).thenReturn(true);

    PlayerBanData ban = testUtils.createBan(player, sender.getData(), "test ban");
    long initialRecordCount = plugin.getPlayerBanRecordStorage().getCount(player);

    String[] args = new String[]{player.getName(), "-d"};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    // Verify no new record was created when using delete flag
    assertEquals(initialRecordCount, plugin.getPlayerBanRecordStorage().getCount(player));
  }

  @Test
  public void shouldFailDeleteWithoutPermission() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    when(sender.hasPermission(cmd.getPermission() + ".delete")).thenReturn(false);

    PlayerBanData ban = testUtils.createBan(player, sender.getData(), "test ban");

    String[] args = new String[]{player.getName(), "-d"};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");

    // Player should still be banned
    assertTrue(plugin.getPlayerBanStorage().isBanned(player.getUUID()));
  }

  @Test
  public void shouldUnbanSilently() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    PlayerBanData ban = testUtils.createBan(player, sender.getData(), "test ban");

    String[] args = new String[]{player.getName(), "-s"};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerBanStorage().isBanned(player.getUUID()));
    assertFalse(plugin.getPlayerBanStorage().isBanned(player.getUUID()));

    // Verify broadcast was not called (silent unban)
    verify(server, never()).broadcast(anyString(), eq("bm.notify.unban"));
  }

  @Test
  public void shouldFailOwnUnbanWithoutPermission() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData otherMod = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create ban by a different actor
    PlayerBanData ban = testUtils.createBan(player, otherMod, "test ban");

    // Sender has "own" permission but not override permission
    when(sender.hasPermission("bm.exempt.override.ban")).thenReturn(false);
    when(sender.hasPermission("bm.command.unban.own")).thenReturn(true);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    // Should fail because sender didn't create the ban and has .own permission
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + player.getName() + " was not banned by you, unable to unban"));
  }
}
