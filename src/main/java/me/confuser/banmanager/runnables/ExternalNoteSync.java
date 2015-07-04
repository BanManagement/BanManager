package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.data.external.ExternalPlayerBanData;
import me.confuser.banmanager.data.external.ExternalPlayerBanRecordData;
import me.confuser.banmanager.data.external.ExternalPlayerNoteData;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.banmanager.storage.PlayerNoteStorage;
import me.confuser.banmanager.storage.external.ExternalPlayerBanRecordStorage;
import me.confuser.banmanager.storage.external.ExternalPlayerBanStorage;
import me.confuser.banmanager.storage.external.ExternalPlayerNoteStorage;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ExternalNoteSync implements Runnable {

  private BanManager plugin = BanManager.getPlugin();
  private ExternalPlayerNoteStorage noteStorage = plugin.getExternalPlayerNoteStorage();
  private PlayerNoteStorage localNoteStorage = plugin.getPlayerNoteStorage();
  private long lastChecked = 0;
  @Getter
  private boolean isRunning = false;

  public ExternalNoteSync() {
    lastChecked = plugin.getSchedulesConfig().getLastChecked("externalPlayerNotes");
  }

  @Override
  public void run() {
    if (isRunning) return;

    isRunning = true;
    // New/updated bans check
    newNotes();

    lastChecked = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastChecked("externalPlayerNotes", lastChecked);
    isRunning = false;
  }


  private void newNotes() {

    CloseableIterator<ExternalPlayerNoteData> itr = null;
    try {
      itr = noteStorage.findNotes(lastChecked);

      while (itr.hasNext()) {
        ExternalPlayerNoteData note = itr.next();

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
