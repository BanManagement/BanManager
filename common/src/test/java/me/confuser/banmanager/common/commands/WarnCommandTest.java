package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.configs.ActionCommand;
import me.confuser.banmanager.common.configs.WarningActionsConfig;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.parsers.WarnCommandParser;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;
import static org.awaitility.Awaitility.await;
import java.lang.reflect.Field;

public class WarnCommandTest extends BasePluginDbTest {
  private WarnCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("warn")) {
        this.cmd = (WarnCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", "confuser", "test"};

    when(sender.hasPermission(cmd.getPermission() + ".silent")).thenReturn(false);

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }

  @Test
  public void shouldFailIfNoPointsPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-p", "5", "confuser", "test"};

    when(sender.hasPermission(cmd.getPermission() + ".points")).thenReturn(false);

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }

  @Test
  public void shouldFailIfSelfWarn() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"Console", "test"};

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou cannot perform that action on yourself!");
  }

  @Test
  public void shouldFailIfOffline() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{testUtils.createRandomPlayerName(), "test"};

    when(sender.hasPermission("bm.command.warn.offline")).thenReturn(false);

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&cYou are not allowed to perform this action on an offline player");
  }

  @Test
  public void shouldFailIfPlayerExempt() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    PlayerData player = testUtils.createRandomPlayer();
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName(), "test"};

    when(sender.hasPermission("bm.exempt.override.warn")).thenReturn(false);
    when(commonPlayer.hasPermission("bm.exempt.warn")).thenReturn(true);

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 1)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldWarnPlayer() throws SQLException {
    PlayerData player = testUtils.createRandomPlayer();
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = spy(server.getConsoleSender());
    String[] args = new String[]{player.getName(), "test"};

    assert (cmd.onCommand(sender, new WarnCommandParser(plugin, args, 1)));

    await().until(() -> plugin.getPlayerWarnStorage().getCount(player) == 1);
    PlayerWarnData data = plugin.getPlayerWarnStorage().getWarnings(player).next();

    assertEquals(player.getName(), data.getPlayer().getName());
    assertEquals("test", data.getReason());
    assertEquals(sender.getName(), data.getActor().getName());
  }

  @Test
  public void shouldTriggerPointsTimeframeWarningActions() throws Exception {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    PlayerData player = testUtils.createRandomPlayer();

    ActionCommand commandWithTimeframe = new ActionCommand("kick", 0, "5m");
    ActionCommand commandWithoutTimeframe = new ActionCommand("ban", 0, "");
    ActionCommand commandWithOldTimeframe = new ActionCommand("kick", 0, "30d");
    WarningActionsConfig warningActionsConfig = spy(plugin.getConfig().getWarningActions());

    PlayerWarnData oldWarning = new PlayerWarnData(player, sender.getData(), "testing", false, 0, DateUtils.parseDateDiff("31d", false));
    plugin.getPlayerWarnStorage().createPreservingTimestamps(oldWarning);
    PlayerWarnData recentWarning = new PlayerWarnData(player, sender.getData(), "testing", false, 0, DateUtils.parseDateDiff("1m", false));
    plugin.getPlayerWarnStorage().createPreservingTimestamps(recentWarning);

    await().until(() -> plugin.getPlayerWarnStorage().getCount(player) == 2);

    HashMap<Double, List<ActionCommand>> actions = new HashMap<>();
    actions.put(1.0, Arrays.asList(commandWithoutTimeframe, commandWithTimeframe, commandWithOldTimeframe));

    Field actionsField = WarningActionsConfig.class.getDeclaredField("actions");
    actionsField.setAccessible(true);
    actionsField.set(warningActionsConfig, actions);

    List<ActionCommand> commands = warningActionsConfig.getCommands(player, 5);

    assertEquals(2, commands.size());

    assertTrue(commands.contains(commandWithTimeframe));
    assertTrue(commands.contains(commandWithOldTimeframe));
    assertFalse(commands.contains(commandWithoutTimeframe));
  }
}
