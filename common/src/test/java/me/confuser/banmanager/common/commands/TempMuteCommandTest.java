package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TempMuteCommandTest extends BasePluginDbTest {
  private TempMuteCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("tempmute")) {
        this.cmd = (TempMuteCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoTimeOrReason() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser", "1d"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
  }

  @Test
  public void shouldFailIfInvalidTimeFormat() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"confuser", "invalid", "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&cYour time length is invalid");
  }

  @Test
  public void shouldTempMutePlayer() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "1h", "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());

    assertEquals(player.getName(), mute.getPlayer().getName());
    assertEquals("test", mute.getReason());
    assertTrue(mute.getExpires() > 0);
  }

  @Test
  public void shouldTempMuteWithVariousTimeFormats() {
    String[] timeFormats = {"30s", "5m", "2h", "1d", "1w"};

    for (String time : timeFormats) {
      PlayerData player = testUtils.createRandomPlayer();
      CommonSender sender = spy(plugin.getServer().getConsoleSender());
      String[] args = new String[]{player.getName(), time, "test"};

      assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
      await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

      PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());
      assertTrue("Expiry should be set for " + time, mute.getExpires() > 0);

      // Clean up
      plugin.getPlayerMuteStorage().removeMute(player.getUUID());
    }
  }

  @Test
  public void shouldTempMuteWithLongDuration() {
    // Test that very long durations are accepted when time limits are not configured
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    String[] args = new String[]{player.getName(), "30y", "test"};
    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    assertTrue(plugin.getPlayerMuteStorage().getMute(player.getUUID()).getExpires() > 0);
  }

  @Test
  public void shouldFailWhenAlreadyMutedWithoutOverride() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // First mute the player
    PlayerMuteData existingMute = testUtils.createMute(player, sender.getData(), "first mute");
    assertTrue(plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    // No override permission
    when(sender.hasPermission("bm.command.tempmute.override")).thenReturn(false);

    String[] args = new String[]{player.getName(), "1h", "second mute"};
    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));

    verify(sender).sendMessage("&c" + player.getName() + " is already muted");

    // Original mute should still be in place
    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());
    assertEquals("first mute", mute.getReason());
  }

  @Test
  public void shouldTempMuteSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "1h", "test", "-s"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    assertTrue(plugin.getPlayerMuteStorage().getMute(player.getUUID()).isSilent());
  }

  @Test
  public void shouldTempMuteSoftly() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "1h", "test", "-st"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    assertTrue(plugin.getPlayerMuteStorage().getMute(player.getUUID()).isSoft());
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", "confuser", "1h", "test"};

    when(sender.hasPermission(cmd.getPermission() + ".silent")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }
}
