package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerNoteData;
import me.confuser.banmanager.common.storage.PlayerNoteStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerNoteStorage;

import java.sql.SQLException;

public class GlobalNoteSync extends BmRunnable {

  private GlobalPlayerNoteStorage noteStorage;
  private PlayerNoteStorage localNoteStorage;

  public GlobalNoteSync(BanManagerPlugin plugin) {
    super(plugin, "externalPlayerNotes");

    noteStorage = plugin.getGlobalPlayerNoteStorage();
    localNoteStorage = plugin.getPlayerNoteStorage();
  }

  @Override
  public void run() {
    newNotes();
  }

  private void newNotes() {

    CloseableIterator<GlobalPlayerNoteData> itr = null;
    try {
      itr = noteStorage.findNotes(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerNoteData note = itr.next();

        final PlayerNoteData localNote = note.toLocal(plugin);

        CloseableIterator<PlayerNoteData> notes = null;
        boolean create = true;

        try {
          notes = localNoteStorage.getNotes(note.getUUID());

          while (create && notes.hasNext()) {
            PlayerNoteData check = notes.next();

            if (check.equalsNote(localNote)) create = false;
          }
        } catch (SQLException e) {
          e.printStackTrace();
          create = false;
        } finally {
          if (notes != null) notes.closeQuietly();
        }

        if (create) localNoteStorage.addNote(localNote);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
