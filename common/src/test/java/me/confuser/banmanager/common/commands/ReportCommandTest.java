package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.TestPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.UUID;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ReportCommandTest extends BasePluginDbTest {
  private ReportCommand cmd;

  @BeforeEach
  public void setupCmd() {
    cmd = requireCommand("report");
  }

  @Test
  public void shouldCreateReport() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = this.server;
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test report reason"};

    boolean result = cmd.onCommand(sender, new CommandParser(plugin, args, 1));
    assertTrue(result);
  }

  @Test
  public void shouldFailIfSelfReport() {
    CommonServer server = this.server;
    CommonSender sender = spy(server.getConsoleSender());

    // Get sender's player data
    PlayerData senderData = sender.getData();
    String[] args = new String[]{senderData.getName(), "trying to report myself"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    await().untilAsserted(() -> verify(sender).sendMessage("&cYou cannot perform that action on yourself!"));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName, "test report"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldFailIfNoReasonGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{"confuser"};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
  }

  @Test
  public void shouldFailIfAmbiguousPartialMatch() {
    PlayerData offlinePlayer = testUtils.createPlayerWithName("Player");
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{offlinePlayer.getName(), "test report"};

    server.setUseStorageForOnlineLookups(false);
    server.clearExactMatches();
    server.clearPartialMatches();
    server.setPartialMatch("Player", new TestPlayer(UUID.randomUUID(), "Player123", true));
    when(sender.hasPermission("bm.command.report.offline")).thenReturn(false);

    try {
      assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
      verify(sender).sendMessage("&cMultiple players match \"" + offlinePlayer.getName() + "\". Please use their full name.");
    } finally {
      server.clearPartialMatches();
      server.clearExactMatches();
      server.setUseStorageForOnlineLookups(true);
    }
  }

  @Test
  public void shouldAllowPartialWhenNoExactCollision() throws SQLException {
    PlayerData targetPlayer = testUtils.createPlayerWithName("Player123");
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"Play", "test report reason"};

    server.setUseStorageForOnlineLookups(false);
    server.clearExactMatches();
    server.clearPartialMatches();
    server.setPartialMatch("Play", new TestPlayer(targetPlayer.getUUID(), targetPlayer.getName(), true));
    when(sender.hasPermission("bm.command.report.offline")).thenReturn(false);

    try {
      assert (cmd.onCommand(sender, new CommandParser(plugin, args, 1)));
      await().until(() -> plugin.getPlayerReportStorage().getCount(targetPlayer) > 0);
      verify(sender, never()).sendMessage("&cYou are not allowed to perform this action on an offline player");
    } finally {
      server.clearPartialMatches();
      server.clearExactMatches();
      server.setUseStorageForOnlineLookups(true);
    }
  }

  @Test
  public void shouldNotifyStaff() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = this.server;
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test report notification"};

    boolean result = cmd.onCommand(sender, new CommandParser(plugin, args, 1));
    assertTrue(result);
  }
}
