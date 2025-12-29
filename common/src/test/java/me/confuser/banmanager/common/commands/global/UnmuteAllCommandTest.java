package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteData;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class UnmuteAllCommandTest extends BasePluginDbTest {
  private UnmuteAllCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand command : plugin.getCommands()) {
      if (command.getCommandName().equals("unmuteall")) {
        this.cmd = (UnmuteAllCommand) command;
        break;
      }
    }
  }

  @Test
  public void shouldRemoveGlobalMute() throws SQLException {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // First create a global mute
    GlobalPlayerMuteData mute = new GlobalPlayerMuteData(player, actor, "test", false);
    plugin.getGlobalPlayerMuteStorage().create(mute);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    // Verify the command was executed
    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    Assume.assumeNotNull(cmd);
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldFailIfNotGloballyMuted() {
    Assume.assumeNotNull(cmd);
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName()};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + player.getName() + " is not muted"));
  }
}
