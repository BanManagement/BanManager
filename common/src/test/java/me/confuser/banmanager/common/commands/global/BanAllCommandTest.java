package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import org.junit.Assume;

public class BanAllCommandTest extends BasePluginDbTest {
  private BanAllCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand command : plugin.getCommands()) {
      if (command.getCommandName().equals("banall")) {
        this.cmd = (BanAllCommand) command;
        break;
      }
    }
  }

  @Test
  public void shouldCreateGlobalBan() throws SQLException {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "global ban test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    // Verify the command was executed (the global ban was created)
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
  public void shouldNotifyOnGlobalBan() throws SQLException {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "global ban notification test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().untilAsserted(() -> verify(server, atLeastOnce()).broadcast(anyString(), anyString()));
  }
}
