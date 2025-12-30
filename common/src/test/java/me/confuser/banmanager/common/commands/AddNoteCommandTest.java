package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AddNoteCommandTest extends BasePluginDbTest {
  private AddNoteCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("addnote")) {
        this.cmd = (AddNoteCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoMessageGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName, "test note"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldAddNoteToPlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test note message"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getPlayerNoteStorage().getCount(player) > 0);
    assertTrue(plugin.getPlayerNoteStorage().getCount(player) > 0);
  }

  @Test
  public void shouldAddNoteSilently() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "silent note", "-s"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().until(() -> plugin.getPlayerNoteStorage().getCount(player) > 0);
    assertTrue(plugin.getPlayerNoteStorage().getCount(player) > 0);

    // Verify broadcast was not called
    verify(server, never()).broadcast(anyString(), eq("bm.notify.notes"));
  }
}



