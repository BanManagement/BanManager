package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

public class NamesCommandTest extends BasePluginDbTest {
  private NamesCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("bmnames")) {
        this.cmd = (NamesCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldShowNameHistory() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{player.getName()};

    // Create a session record (which includes name)
    testUtils.createSession(player, true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldShowNoHistoryMessage() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{player.getName()};

    // Don't create any name history

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(contains("No name history")));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    await().untilAsserted(() -> verify(sender).sendMessage(contains("not found")));
  }

}
