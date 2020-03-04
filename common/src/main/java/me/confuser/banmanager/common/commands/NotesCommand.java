package me.confuser.banmanager.common.commands;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class NotesCommand extends CommonCommand {

  public NotesCommand(BanManagerPlugin plugin) {
    super(plugin, "notes", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length == 0 && !sender.hasPermission("bm.command.notes.online")) {
      Message.get("sender.error.noPermission").sendTo(sender);
      return true;
    }

    if (parser.args.length > 1) {
      return false;
    }

    if (parser.args.length == 1) {
      final String name = parser.args[0];

      getPlugin().getScheduler().runAsync(() -> {
        PlayerData player = getPlugin().getPlayerStorage().retrieve(name, false);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", name).toString());
          return;
        }

        CloseableIterator<PlayerNoteData> notesItr = null;

        try {
          notesItr = getPlugin().getPlayerNoteStorage().getNotes(player.getUUID());
          ArrayList<Message> notes = new ArrayList<>();
          String dateTimeFormat = Message.getString("notes.dateTimeFormat");

          while (notesItr.hasNext()) {
            PlayerNoteData note = notesItr.next();

            Message noteMessage = Message.get("notes.note")
                .set("player", note.getActor().getName())
                .set("message", note.getMessage())
                .set("created", DateUtils.format(dateTimeFormat, note.getCreated()));
            notes.add(noteMessage);
          }

          if (notes.size() == 0) {
            Message.get("notes.error.noNotes").set("player", player.getName()).sendTo(sender);
            return;
          }

          Message header = Message.get("notes.header")
              .set("player", player.getName());

          header.sendTo(sender);

          for (Message message : notes) {
            message.sendTo(sender);
          }

        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          if (notesItr != null) notesItr.closeQuietly();
        }
      });
    } else {
      getPlugin().getScheduler().runAsync(new Runnable() {

        @Override
        public void run() {
          CommonPlayer[] onlinePlayers = getPlugin().getServer().getOnlinePlayers();

          if (onlinePlayers.length == 0) {
            Message.get("notes.error.noOnlineNotes").sendTo(sender);
            return;
          }

          CloseableIterator<PlayerNoteData> notesItr = null;

          try {
            notesItr = getPlugin().getPlayerNoteStorage()
                .queryBuilder()
                .where()
                .in("player_id", Arrays.stream(onlinePlayers).map(player -> UUIDUtils.toBytes(player
                    .getUniqueId())).collect(Collectors.toList()))
                .iterator();
            ArrayList<Message> notes = new ArrayList<>();
            String dateTimeFormat = Message.getString("notes.dateTimeFormat");

            while (notesItr.hasNext()) {
              PlayerNoteData note = notesItr.next();

              Message noteMessage = Message.get("notes.playerNote")
                  .set("player", note.getPlayer().getName())
                  .set("actor", note.getActor().getName())
                  .set("message", note.getMessage())
                  .set("created", DateUtils.format(dateTimeFormat, note.getCreated()));
              notes.add(noteMessage);
            }

            if (notes.size() == 0) {
              Message.get("notes.error.noOnlineNotes").sendTo(sender);
              return;
            }

            for (Message message : notes) {
              message.sendTo(sender);
            }

          } catch (SQLException e) {
            e.printStackTrace();
          } finally {
            if (notesItr != null) notesItr.closeQuietly();
          }
        }
      });
    }

    return true;
  }

  public static TextComponent notesAmountMessage(String playerName, Message text) {
    return LegacyComponentSerializer.legacy().deserialize(
        text.set("player", playerName).toString(), '&')
        .clickEvent(ClickEvent.runCommand("/notes " + playerName));
  }
}
