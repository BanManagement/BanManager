package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MuteAllCommandTest extends BasePluginDbTest {
  private MuteAllCommand cmd;

  @BeforeEach
  public void setupCmd() {
    cmd = requireCommand("muteall");
  }

  @Test
  public void shouldCreateGlobalMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = this.server;
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "global mute test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName, "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldNotifyOnGlobalMute() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = this.server;
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "global mute notification test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));

    await().untilAsserted(() -> verify(server, atLeastOnce()).broadcast(anyString(), anyString()));
  }
}
