package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.Message;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NoteListenerTest extends BasePluginDbTest {
  @Test
  public void shouldBroadcast() {
    PlayerData player = testUtils.createRandomPlayer();
    BanManagerPlugin plugin = spy(this.plugin);
    CommonServer server = this.server;
    CommonSender sender = plugin.getServer().getConsoleSender();
    PlayerNoteData data = new PlayerNoteData(player, sender.getData(), "test");
    CommonPlayer testPlayer = spy(new TestPlayer(sender.getData().getUUID(), sender.getName(), false));
    Message expected = Message.get("notes.notify");

    expected.set("player", data.getPlayer().getName())
        .set("playerId", data.getPlayer().getUUID().toString())
        .set("actor", data.getActor().getName())
        .set("message", data.getMessage());

    when(plugin.getServer()).thenReturn(server);
    when(server.getPlayer(sender.getData().getUUID())).thenReturn(testPlayer);
    when(testPlayer.hasPermission("bm.notify.notes")).thenReturn(false);

    CommonNoteListener listener = new CommonNoteListener(plugin);
    listener.notifyOnNote(data);

    ArgumentCaptor<Message> broadcastCaptor = ArgumentCaptor.forClass(Message.class);
    verify(server).broadcast(broadcastCaptor.capture(), eq("bm.notify.notes"));
    assertEquals(expected.toString(), broadcastCaptor.getValue().toString());

    ArgumentCaptor<Message> playerCaptor = ArgumentCaptor.forClass(Message.class);
    verify(testPlayer).sendMessage(playerCaptor.capture());
    assertEquals(expected.toString(), playerCaptor.getValue().toString());
  }
}
