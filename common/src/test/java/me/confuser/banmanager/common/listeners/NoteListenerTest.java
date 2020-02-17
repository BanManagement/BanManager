package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.Message;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class NoteListenerTest extends BasePluginDbTest {
  @Test
  public void shouldBroadcast() {
    PlayerData player = testUtils.createRandomPlayer();
    BanManagerPlugin plugin = spy(this.plugin);
    CommonServer server = spy(plugin.getServer());
    CommonSender sender = plugin.getServer().getConsoleSender();
    PlayerNoteData data = new PlayerNoteData(player, sender.getData(), "test");
    CommonPlayer testPlayer = spy(new TestPlayer(sender.getData().getUUID(), sender.getName(), false));
    Message message = Message.get("notes.notify");

    message.set("player", data.getPlayer().getName())
        .set("playerId", data.getPlayer().getUUID().toString())
        .set("actor", data.getActor().getName())
        .set("message", data.getMessage());

    when(plugin.getServer()).thenReturn(server);
    when(server.getPlayer(sender.getData().getUUID())).thenReturn(testPlayer);
    when(testPlayer.hasPermission("bm.notify.notes")).thenReturn(false);

    CommonNoteListener listener = new CommonNoteListener(plugin);
    listener.notifyOnNote(data);

    verify(server).broadcast(message.toString(), "bm.notify.notes");
    verify(testPlayer).sendMessage(message);
  }
}
