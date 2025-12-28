package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.util.parsers.UnbanCommandParser;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UnmuteCommandTest extends BasePluginDbTest {
  private UnmuteCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("unmute")) {
        this.cmd = (UnmuteCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNotMuted() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{player.getName()};

    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&c" + player.getName() + " is not muted");
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName};

    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&c" + playerName + " is not muted");
  }

  @Test
  public void shouldUnmutePlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // First mute the player
    PlayerMuteData mute = testUtils.createMute(player, sender.getData(), "test mute");
    assertTrue(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    assertFalse(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    // Verify record was created
    assertTrue(plugin.getPlayerMuteRecordStorage().getCount(player) > 0);
  }

  @Test
  public void shouldUnmuteByUUID() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    PlayerMuteData mute = testUtils.createMute(player, sender.getData(), "test mute");
    assertTrue(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    String[] args = new String[]{player.getUUID().toString()};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    assertFalse(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
  }

  @Test
  public void shouldUnmuteWithDeleteFlag() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    when(sender.hasPermission(cmd.getPermission() + ".delete")).thenReturn(true);

    PlayerMuteData mute = testUtils.createMute(player, sender.getData(), "test mute");
    long initialRecordCount = plugin.getPlayerMuteRecordStorage().getCount(player);

    String[] args = new String[]{player.getName(), "-d"};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    // Verify no new record was created when using delete flag
    assertEquals(initialRecordCount, plugin.getPlayerMuteRecordStorage().getCount(player));
  }

  @Test
  public void shouldFailDeleteWithoutPermission() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    when(sender.hasPermission(cmd.getPermission() + ".delete")).thenReturn(false);

    PlayerMuteData mute = testUtils.createMute(player, sender.getData(), "test mute");

    String[] args = new String[]{player.getName(), "-d"};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");

    // Player should still be muted
    assertTrue(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
  }

  @Test
  public void shouldUnmuteSilently() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    PlayerMuteData mute = testUtils.createMute(player, sender.getData(), "test mute");

    String[] args = new String[]{player.getName(), "-s"};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    await().until(() -> !plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    assertFalse(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    // Verify broadcast was not called (silent unmute)
    verify(server, never()).broadcast(anyString(), eq("bm.notify.unmute"));
  }

  @Test
  public void shouldFailOwnUnmuteWithoutPermission() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData otherMod = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create mute by a different actor
    PlayerMuteData mute = testUtils.createMute(player, otherMod, "test mute");

    // Sender has "own" permission but not override permission
    when(sender.hasPermission("bm.exempt.override.mute")).thenReturn(false);
    when(sender.hasPermission("bm.command.unmute.own")).thenReturn(true);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new UnbanCommandParser(plugin, args, 0)));

    // Should fail because sender didn't create the mute and has .own permission
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + player.getName() + " was not muted by you, unable to unmute"));
  }
}
