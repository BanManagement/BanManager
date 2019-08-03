package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerNoteData;
import me.confuser.banmanager.storage.PlayerNoteStorage;
import me.confuser.banmanager.storage.global.GlobalPlayerNoteStorage;

import java.sql.SQLException;

public class GlobalNoteSync extends BmRunnable {

  private GlobalPlayerNoteStorage noteStorage = plugin.getGlobalPlayerNoteStorage();
  private PlayerNoteStorage localNoteStorage = plugin.getPlayerNoteStorage();

  public GlobalNoteSync() {
    super("externalPlayerNotes");
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

        final PlayerNoteData localNote = note.toLocal();

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
