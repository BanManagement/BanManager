package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class KickAllCommandTest extends BasePluginDbTest {
  private KickAllCommand cmd;

  @Before
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("kickall")) {
        this.cmd = (KickAllCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", "test"};

    when(sender.hasPermission(cmd.getPermission() + ".silent")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }

  @Test
  public void shouldFailIfExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{"test"};

    when(sender.hasPermission("bm.exempt.override.kick")).thenReturn(false);
    when(commonPlayer.hasPermission("bm.exempt.kick")).thenReturn(true);
    when(server.getOnlinePlayers()).thenReturn(new CommonPlayer[]{commonPlayer});

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    verify(commonPlayer, never()).kick("&6You have been kicked");
    verify(server).broadcast("All players have been kicked by Console for &4test", "bm.notify.kick");
  }

  @Test
  public void shouldKickPlayerWithoutAReason() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getOnlinePlayers()).thenReturn(new CommonPlayer[]{commonPlayer});

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    verify(commonPlayer).kick("&6You have been kicked");
    verify(server).broadcast("All players have been kicked by Console", "bm.notify.kick");
  }

  @Test
  public void shouldKickPlayerWithAReason() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{"test"};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getOnlinePlayers()).thenReturn(new CommonPlayer[]{commonPlayer});

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    verify(commonPlayer).kick("&6You have been kicked for &4" + args[0]);
    verify(server).broadcast("All players have been kicked by Console for &4" + args[0], "bm.notify.kick");
  }

  @Test
  public void shouldKickPlayerWithoutAReasonSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{"-s"};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getOnlinePlayers()).thenReturn(new CommonPlayer[]{commonPlayer});

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    verify(commonPlayer).kick("&6You have been kicked");
    verify(server, never()).broadcast("All players have been kicked by Console", "bm.notify.kick");
  }

  @Test
  public void shouldKickPlayerWithAReasonSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{"-s", "test", "reason"};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getOnlinePlayers()).thenReturn(new CommonPlayer[]{commonPlayer});

    assert (cmd.onCommand(sender, new CommandParser(plugin, args, 0)));

    verify(commonPlayer).kick("&6You have been kicked for &4test reason");
    verify(server, never()).broadcast("All players have been kicked by Console for &4test reason", "bm.notify.kick");
  }
}
