package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ReportCommandTest extends BasePluginDbTest {
  private ReportCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand command : plugin.getCommands()) {
      if (command.getCommandName().equals("report")) {
        this.cmd = (ReportCommand) command;
        break;
      }
    }
  }

  @Test
  public void shouldCreateReport() throws SQLException {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test report reason"};

    boolean result = cmd.onCommand(sender, new CommandParser(plugin, args, 1));
    assertTrue(result);
  }

  @Test
  public void shouldFailIfSelfReport() {
    Assume.assumeNotNull(cmd);
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Get sender's player data
    PlayerData senderData = sender.getData();
    String[] args = new String[]{senderData.getName(), "trying to report myself"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    await().untilAsserted(() -> verify(sender).sendMessage("&cYou cannot perform that action on yourself!"));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    Assume.assumeNotNull(cmd);
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName, "test report"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    Assume.assumeNotNull(cmd);
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldNotifyStaff() throws SQLException {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test report notification"};

    boolean result = cmd.onCommand(sender, new CommandParser(plugin, args, 1));
    assertTrue(result);
  }
}
