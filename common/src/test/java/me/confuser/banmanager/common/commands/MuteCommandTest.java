package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.configs.ExemptionsConfig;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

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
    String[] args = new String[]{StringUtils.truncate(faker.name().username(), 16), "test"};

    when(sender.hasPermission(cmd.getPermission() + ".offline")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou are not allowed to perform this action on an offline player");
  }

  @Test
  public void shouldFailIfExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
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
    ExemptionsConfig config = spy(plugin.getExemptionsConfig());
    String[] args = new String[]{player.getName(), "test"};

    when(sender.hasPermission("bm.exempt.override.mute")).thenReturn(false);
    when(config.isExempt(player, "mute")).thenReturn(true);

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