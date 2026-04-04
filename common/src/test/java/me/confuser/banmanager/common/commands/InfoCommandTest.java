package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.parsers.InfoCommandParser;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class InfoCommandTest extends BasePluginDbTest {
  private InfoCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("bminfo")) {
        this.cmd = (InfoCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldShowPlayerInfo() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName()};

    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldShowBanHistory() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create a ban and unban to create history
    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");
    plugin.getPlayerBanStorage().unban(ban, actor, "unbanned");

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldShowMuteHistory() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create a mute and unmute to create history
    PlayerMuteData mute = testUtils.createMute(player, actor, "test mute");
    plugin.getPlayerMuteStorage().unmute(mute, actor, "unmuted");

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldShowWarnings() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create a warning
    PlayerWarnData warning = new PlayerWarnData(player, actor, "test warning", 1.0);
    plugin.getPlayerWarnStorage().addWarning(warning, false);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldShowNotes() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create a note
    PlayerNoteData note = new PlayerNoteData(player, actor, "test note");
    plugin.getPlayerNoteStorage().create(note);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }

  @Test
  public void shouldFailIfPlayerNotFound() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String playerName = testUtils.createRandomPlayerName();
    String[] args = new String[]{playerName};

    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));
    await().untilAsserted(() -> verify(sender).sendMessage("&c" + playerName + " not found, are you sure they exist?"));
  }

  @Test
  public void shouldShowKnownNames() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create a session record (which includes name)
    testUtils.createSession(player, true);

    when(sender.hasPermission("bm.command.bminfo.names")).thenReturn(true);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(contains("Known names")));
  }

  @Test
  public void shouldHideNamesWithoutPermission() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    // Create a session record (which includes name)
    testUtils.createSession(player, true);

    when(sender.hasPermission("bm.command.bminfo.names")).thenReturn(false);

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, never()).sendMessage(contains("Known names")));
  }

  @Test
  public void shouldShowStatsWithMultipleRecordTypes() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    PlayerData actor = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());

    PlayerBanData ban = testUtils.createBan(player, actor, "test ban");
    plugin.getPlayerBanStorage().unban(ban, actor, "unbanned");

    PlayerMuteData mute = testUtils.createMute(player, actor, "test mute");
    plugin.getPlayerMuteStorage().unmute(mute, actor, "unmuted");

    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player, actor, "warn 1", 3.0), false);
    plugin.getPlayerWarnStorage().addWarning(new PlayerWarnData(player, actor, "warn 2", 2.0), false);

    plugin.getPlayerNoteStorage().create(new PlayerNoteData(player, actor, "a note"));

    ReportState openState = plugin.getReportStateStorage().queryForId(1);
    plugin.getPlayerReportStorage().create(new PlayerReportData(player, actor, "report", openState));

    String[] args = new String[]{player.getName()};
    assert (cmd.onCommand(sender, new InfoCommandParser(plugin, args)));

    await().untilAsserted(() -> verify(sender, atLeastOnce()).sendMessage(anyString()));
  }
}
