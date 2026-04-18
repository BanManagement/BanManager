package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginDbTest;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KickCommandTest extends BasePluginDbTest {
  private KickCommand cmd;

  @BeforeEach
  public void setupCmd() {
    for (CommonCommand cmd : plugin.getCommands()) {
      if (cmd.getCommandName().equals("kick")) {
        this.cmd = (KickCommand) cmd;
        break;
      }
    }
  }

  @Test
  public void shouldFailIfNoPlayerGiven() {
    CommonSender sender = plugin.getServer().getConsoleSender();
    String[] args = new String[]{};

    assertFalse(cmd.onCommand(sender, new CommandParser(plugin, args)));
  }

  @Test
  public void shouldFailIfNoSilentPermission() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"-s", player.getName(), "test"};

    when(sender.hasPermission(cmd.getPermission() + ".silent")).thenReturn(false);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));
    verify(sender).sendMessage("&cYou do not have permission to perform that action");
  }

  @Test
  public void shouldFailIfSelf() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{"Console", "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));
    verify(sender).sendMessage("&cYou cannot perform that action on yourself!");
  }

  @Test
  public void shouldFailIfOffline() {
    CommonSender sender = spy(plugin.getServer().getConsoleSender());
    String[] args = new String[]{testUtils.createRandomPlayerName(), "test"};

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));
    verify(sender).sendMessage("&c" + args[0] + " is offline");
  }

  @Test
  public void shouldFailIfExempt() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName(), "test"};

    when(sender.hasPermission("bm.exempt.override.kick")).thenReturn(false);
    when(commonPlayer.hasPermission("bm.exempt.kick")).thenReturn(true);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));
    verify(sender).sendMessage("&c" + player.getName() + " is exempt from that action");
  }

  @Test
  public void shouldKickPlayerWithoutAReason() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName()};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getPlayer(args[0])).thenReturn(commonPlayer);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));

    verify(commonPlayer).kick("&6You have been kicked");

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(server).broadcast(msgCaptor.capture(), eq("bm.notify.kick"));
    assertEquals("&6" + args[0] + " has been kicked by Console &3[Info]", msgCaptor.getValue().toString());
  }

  @Test
  public void shouldKickPlayerWithAReason() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName(), "test reason"};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getPlayer(args[0])).thenReturn(commonPlayer);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));

    verify(commonPlayer).kick("&6You have been kicked for &4" + args[1]);

    ArgumentCaptor<Message> msgCaptor = ArgumentCaptor.forClass(Message.class);
    verify(server).broadcast(msgCaptor.capture(), eq("bm.notify.kick"));
    assertEquals("&6" + args[0] + " has been kicked by Console for &4" + args[1] + " &3[Info]", msgCaptor.getValue().toString());
  }

  @Test
  public void shouldKickPlayerWithoutAReasonSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName(), "-s"};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getPlayer(args[0])).thenReturn(commonPlayer);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));

    verify(commonPlayer).kick("&6You have been kicked");
    verify(server, never()).broadcast(any(Message.class), eq("bm.notify.kick"));
  }

  @Test
  public void shouldKickPlayerWithAReasonSilently() {
    PlayerData player = testUtils.createRandomPlayer();
    CommonSender sender = spy(server.getConsoleSender());
    CommonPlayer commonPlayer = spy(server.getPlayer(player.getName()));
    String[] args = new String[]{player.getName(), "-s", "test", "reason"};

    when(commonPlayer.hasPermission("bm.notify.kick")).thenReturn(false);
    when(server.getPlayer(args[0])).thenReturn(commonPlayer);

    assert (cmd.onCommand(sender, new CommandParser(plugin, args)));

    verify(commonPlayer).kick("&6You have been kicked for &4test reason");
    verify(server, never()).broadcast(any(Message.class), eq("bm.notify.kick"));
  }
}
