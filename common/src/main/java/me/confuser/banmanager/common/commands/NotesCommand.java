package me.confuser.banmanager.common.commands;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerNoteData;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NotesCommand extends SingleCommand {

  public NotesCommand(LocaleManager locale) {
    super(CommandSpec.NOTES.localize(locale), "notes", CommandPermission.NOTES, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {

    if (args.size() == 0 && !sender.hasPermission("bm.command.notes.online")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.size() > 1) {
      return CommandResult.INVALID_ARGS;
    }

    if (args.size() == 1) {
      final String name = args.get(0);

      plugin.getBootstrap().getScheduler().executeAsync(() -> {
        PlayerData player = plugin.getPlayerStorage().retrieve(name, false);

        if (player == null) {
          Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", name);
          return;
        }

        CloseableIterator<PlayerNoteData> notesItr = null;

        try {
          notesItr = plugin.getPlayerNoteStorage().getNotes(player.getUUID());
          ArrayList<String> notes = new ArrayList<>();
          String dateTimeFormat = Message.NOTES_DATETIMEFORMAT.getMessage();
          FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

          while (notesItr.hasNext()) {
            PlayerNoteData note = notesItr.next();

            String noteMessage = Message.NOTES_NOTE.asString(plugin.getLocaleManager(),
                                         "player", note.getActor().getName(),
                                         "message", note.getMessageColours(),
                                         "created", dateFormatter.format(note.getCreated() * 1000L));
            notes.add(noteMessage);
          }

          if (notes.size() == 0) {
            Message.NOTES_ERROR_NONOTES.send(sender, "player", player.getName());
            return;
          }

          Message.NOTES_HEADER.send(sender, "player", player.getName());

          for (String message : notes) {
            sender.sendMessage(message);
          }

        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          if (notesItr != null) notesItr.closeQuietly();
        }
      });
    } else {
      plugin.getBootstrap().getScheduler().executeAsync(() -> {//Should this be async?
        List<UUID> onlineUUIDS = plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList());

        if (onlineUUIDS.size() == 0) {
          Message.NOTES_ERROR_NOONLINENOTES.send(sender);
          return;
        }

        CloseableIterator<PlayerNoteData> notesItr = null;

        try {
          notesItr = plugin.getPlayerNoteStorage()
                           .queryBuilder()
                           .where()
                           .in("player_id", plugin.getPlayerStorage().getOnlineIds(onlineUUIDS))
                           .iterator();
          ArrayList<String> notes = new ArrayList<>();
          String dateTimeFormat = Message.NOTES_DATETIMEFORMAT.getMessage();
          FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

          while (notesItr.hasNext()) {
            PlayerNoteData note = notesItr.next();

            String noteMessage = Message.NOTES_PLAYERNOTE.asString(plugin.getLocaleManager(),
                                         "player", note.getPlayer().getName(),
                                         "actor", note.getActor().getName(),
                                         "message", note.getMessageColours(),
                                         "created", dateFormatter.format(note.getCreated() * 1000L));
            notes.add(noteMessage);
          }

          if (notes.size() == 0) {
            Message.NOTES_ERROR_NOONLINENOTES.send(sender);
            return;
          }

          for (String message : notes) {
            sender.sendMessage(message);
          }

        } catch (SQLException e) {
          e.printStackTrace();
        } finally {
          if (notesItr != null) notesItr.closeQuietly();
        }
      });
    }

    return CommandResult.SUCCESS;
  }

}
