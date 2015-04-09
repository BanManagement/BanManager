package me.confuser.banmanager.commands;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;

public class NotesCommand extends AutoCompleteNameTabCommand<BanManager> {

  public NotesCommand() {
    super("notes");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length != 1) {
      return false;
    }

    final String name = args[0];

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerData player = plugin.getPlayerStorage().retrieve(name, false);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", name).toString());
          return;
        }

        CloseableIterator<PlayerNoteData> notesItr = null;

        try {
          notesItr = plugin.getPlayerNoteStorage().getNotes(player);
          ArrayList<Message> notes = new ArrayList<>();

          while (notesItr.hasNext()) {
            PlayerNoteData note = notesItr.next();

            Message noteMessage = Message.get("notes.note")
                                         .set("player", note.getActor().getName())
                                         .set("message", note.getMessage());
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
          notesItr.closeQuietly();
        }
      }

    });

    return true;
  }
}
