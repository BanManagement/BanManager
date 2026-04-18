package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UnbanAllCommandTest extends BasePluginDbTest {
  private UnbanAllCommand cmd;

  @BeforeEach
  public void setupCmd() {
    cmd = requireCommand("unbanall");
  }

  @Test
  public void shouldRemoveGlobalBan() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    CommonServer server = this.server;
    CommonSender sender = spy(server.getConsoleSender());

    GlobalPlayerBanData ban = new GlobalPlayerBanData(player, actor, "test");
    plugin.getGlobalPlayerBanStorage().create(ban);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldFailIfNotGloballyBanned() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = this.server;
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName()};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + player.getName() + " is not banned"));
  }
}
