package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerNoteData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.storage.PlayerNoteStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerNoteStorage;

import java.sql.SQLException;

public class GlobalNoteSync extends BmRunnable {

  private GlobalPlayerNoteStorage noteStorage;
  private GlobalLocalApplyHelper applyHelper;

  public GlobalNoteSync(BanManagerPlugin plugin) {
    super(plugin, "externalPlayerNotes");

    noteStorage = plugin.getGlobalPlayerNoteStorage();
    applyHelper = new GlobalLocalApplyHelper(plugin);
  }

  @Override
  protected DatabaseConfig getCheckpointDbConfig() {
    return plugin.getConfig().getGlobalDb();
  }

  @Override
  protected BaseDaoImpl<?, ?> getCheckpointDao() {
    return noteStorage;
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

        applyHelper.applyNote(note, true);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
