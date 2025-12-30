package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.parsers.WarnCommandParser;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TempWarnCommandTest extends BasePluginDbTest {
  private TempWarnCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("tempwarn")) {
        this.cmd = (TempWarnCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldTempWarnPlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "1h", "test warning"};

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerWarnStorage().getCount(player) > 0);

    PlayerWarnData warning = plugin.getPlayerWarnStorage().getWarnings(player).next();
    assertEquals("test warning", warning.getReason());
    assertTrue(warning.getExpires() > 0);
  }

  @Test
  public void shouldTempWarnWithPoints() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{"-p", "5", player.getName(), "1h", "test warning"};

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 2)));

    await().until(() -> plugin.getPlayerWarnStorage().getCount(player) > 0);

    PlayerWarnData warning = plugin.getPlayerWarnStorage().getWarnings(player).next();
    assertEquals(5.0, warning.getPoints(), 0.01);
  }

  @Test
  public void shouldFailIfNoTimeGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser", "test"};

    assertFalse(cmd.onCommand(sender, new WarnCommandParser(plugin, args, 2)));
  }

  @Test
  public void shouldFailIfInvalidTimeFormat() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"confuser", "invalid", "test"};

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 2)));
    verify(sender).sendMessage("&cYour time length is invalid");
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName, "1h", "test"};

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 2)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }
}



