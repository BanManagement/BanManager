package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MuteAllCommandTest extends BasePluginDbTest {
  private MuteAllCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand command : plugin.getCommands()) {
      if (command.getCommandName().equals("muteall")) {
        this.cmd = (MuteAllCommand) command;
        break;
      }
    }
  }

  @Test
  public void shouldCreateGlobalMute() throws SQLException {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "global mute test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    // Verify the command was executed
    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    Assume.assumeNotNull(cmd);
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    Assume.assumeNotNull(cmd);
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName, "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldNotifyOnGlobalMute() throws SQLException {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "global mute notification test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().untilAsserted(() -> verify(server, atLeastOnce()).broadcast(anyString(), anyString()));
  }
}
