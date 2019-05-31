package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.data.global.GlobalPlayerMuteRecordData;
import me.confuser.banmanager.storage.PlayerMuteStorage;
import me.confuser.banmanager.storage.global.GlobalPlayerMuteRecordStorage;
import me.confuser.banmanager.storage.global.GlobalPlayerMuteStorage;

import java.sql.SQLException;

public class GlobalMuteSync extends BmRunnable {

  private GlobalPlayerMuteStorage muteStorage = plugin.getGlobalPlayerMuteStorage();
  private PlayerMuteStorage localMuteStorage = plugin.getPlayerMuteStorage();
  private GlobalPlayerMuteRecordStorage recordStorage = plugin.getGlobalPlayerMuteRecordStorage();

  public GlobalMuteSync() {
    super("externalPlayerMutes");
  }

  @Override
  public void run() {
    newUnmutes();
    newMutes();
  }

  private void newMutes() {

    CloseableIterator<GlobalPlayerMuteData> itr = null;
    try {
      itr = muteStorage.findMutes(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerMuteData mute = itr.next();

        final PlayerMuteData localMute = mute.toLocal();

        if (localMuteStorage.retrieveMute(mute.getUUID()) != null) {
          // Global mute overrides local
          localMuteStorage
                  .unmute(localMute, mute.getActor());
        } else if (localMuteStorage.isMuted(mute.getUUID())) {
          localMuteStorage.removeMute(mute.getUUID());
        }

        localMuteStorage.mute(localMute, false);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  private void newUnmutes()  {

    CloseableIterator<GlobalPlayerMuteRecordData> itr = null;
    try {
      itr = recordStorage.findUnmutes(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerMuteRecordData record = itr.next();

        if (!localMuteStorage.isMuted(record.getUUID())) {
          continue;
        }

        localMuteStorage.unmute(localMuteStorage.getMute(record.getUUID()), record.getActor());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }
  }
}
