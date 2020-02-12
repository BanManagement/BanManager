package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BanNameCommandTest extends BasePluginDbTest {
  private BanNameCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("banname")) {
        this.cmd = (BanNameCommand) cmd;
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
  public void shouldFailIfAlreadyBanned() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{player.getName(), "test"};

    assert plugin.getNameBanStorage().ban(new NameBanData(player.getName(), sender.getData(), args[1], true));

    when(sender.hasPermission(cmd.getPermission() + ".override")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cName " + player.getName() + " is already banned");
  }

  @Test
  public void shouldBanPlayerName() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getNameBanStorage().isBanned(player.getName()));
    NameBanData ban = plugin.getNameBanStorage().getBan(player.getName());

    assertEquals(player.getName(), ban.getName());
    assertEquals("test", ban.getReason());
    assertEquals(sender.getName(), ban.getActor().getName());
    assertFalse(ban.isSilent());
  }

  @Test
  public void shouldBanNameSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test", "-s"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getNameBanStorage().isBanned(player.getName()));

    assertTrue(plugin.getNameBanStorage().getBan(player.getName()).isSilent());
  }
}