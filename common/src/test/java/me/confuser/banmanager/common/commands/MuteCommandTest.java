package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.TestPlayer;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MuteCommandTest extends BasePluginDbTest {
  private MuteCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("mute")) {
        this.cmd = (MuteCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", "confuser", "test"};

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
  public void shouldFailIfAlreadyMuted() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{player.getName(), "test"};

    assert plugin.getPlayerMuteStorage().mute(new PlayerMuteData(player, sender.getData(), args[1], true, false));

    when(sender.hasPermission(cmd.getPermission() + ".override")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + player.getName() + " is already muted");
  }

  @Test
  public void shouldFailIfOffline() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{testUtils.createRandomPlayerName(), "test"};

    when(sender.hasPermission(cmd.getPermission() + ".offline")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou are not allowed to perform this action on an offline player");
  }

  @Test
  public void shouldFailIfAmbiguousPartialMatch() {
    PlayerData offlinePlayer = testUtils.createPlayerWithName("Player");
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{offlinePlayer.getName(), "test"};

    server.setUseStorageForOnlineLookups(false);
    server.clearExactMatches();
    server.clearPartialMatches();
    server.setPartialMatch("Player", new TestPlayer(UUID.randomUUID(), "Player123", true));
    when(sender.hasPermission(cmd.getPermission() + ".offline")).thenReturn(false);

    try {
      assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
      verify(sender).sendMessage("&cMultiple players match \"" + offlinePlayer.getName() + "\". Please use their full name.");
    } finally {
      server.clearPartialMatches();
      server.clearExactMatches();
      server.setUseStorageForOnlineLookups(true);
    }
  }

  @Test
  public void shouldAllowPartialWhenNoExactCollision() {
    PlayerData targetPlayer = testUtils.createPlayerWithName("Player123");
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"Play", "test"};

    server.setUseStorageForOnlineLookups(false);
    server.clearExactMatches();
    server.clearPartialMatches();
    server.setPartialMatch("Play", new TestPlayer(targetPlayer.getUUID(), targetPlayer.getName(), true));
    when(sender.hasPermission(cmd.getPermission() + ".offline")).thenReturn(false);

    try {
      assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
      await().until(() -> plugin.getPlayerMuteStorage().isMuted(targetPlayer.getUUID()));
      verify(sender, never()).sendMessage("&cYou are not allowed to perform this action on an offline player");
    } finally {
      server.clearPartialMatches();
      server.clearExactMatches();
      server.setUseStorageForOnlineLookups(true);
    }
  }

  @Test
  public void shouldFailIfExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(new TestPlayer(player.getUUID(), player.getName(), true));
    this.server.setExactMatch(player.getName(), commonPlayer);
    String[] args = new String[]{player.getName(), "test"};

    when(sender.hasPermission("bm.exempt.override.mute")).thenReturn(false);
    when(commonPlayer.hasPermission("bm.exempt.mute")).thenReturn(true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldFailIfNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{faker.internet().uuid(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + args[0] + " not found, are you sure they exist?");
  }

  @Test
  public void shouldFailIfOfflineExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(new TestPlayer(player.getUUID(), player.getName(), true));
    this.server.setExactMatch(player.getName(), commonPlayer);
    String[] args = new String[]{player.getName(), "test"};

    when(sender.hasPermission("bm.exempt.override.mute")).thenReturn(false);
    when(commonPlayer.hasPermission("bm.exempt.mute")).thenReturn(true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldMutePlayer() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());

    assertEquals(player.getName(), mute.getPlayer().getName());
    assertEquals("test", mute.getReason());
    assertEquals(sender.getName(), mute.getActor().getName());
    assertFalse(mute.isSilent());
  }

  @Test
  public void shouldMutePlayerSoftly() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test", "-st"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));
    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());

    assertEquals(player.getName(), mute.getPlayer().getName());
    assertEquals("test", mute.getReason());
    assertEquals(sender.getName(), mute.getActor().getName());
    assertFalse(mute.isSilent());
    assertTrue(mute.isSoft());
  }

  @Test
  public void shouldMutePlayerSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test", "-s"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getPlayerMuteStorage().isMuted(player.getUUID()));

    assertTrue(plugin.getPlayerMuteStorage().getMute(player.getUUID()).isSilent());
  }
}