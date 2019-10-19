package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteRecordData;
import me.confuser.banmanager.common.storage.PlayerMuteStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerMuteRecordStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerMuteStorage;

import java.sql.SQLException;

public class GlobalMuteSync extends BmRunnable {

  private GlobalPlayerMuteStorage muteStorage;
  private PlayerMuteStorage localMuteStorage;
  private GlobalPlayerMuteRecordStorage recordStorage;

  public GlobalMuteSync(BanManagerPlugin plugin) {
    super(plugin, "externalPlayerMutes");

    localMuteStorage = plugin.getPlayerMuteStorage();
    muteStorage = plugin.getGlobalPlayerMuteStorage();
    recordStorage = plugin.getGlobalPlayerMuteRecordStorage();
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

        final PlayerMuteData localMute = mute.toLocal(plugin);

        if (localMuteStorage.retrieveMute(mute.getUUID()) != null) {
          // Global mute overrides local
          localMuteStorage
                  .unmute(localMute, mute.getActor(plugin));
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

  private void newUnmutes() {

    CloseableIterator<GlobalPlayerMuteRecordData> itr = null;
    try {
      itr = recordStorage.findUnmutes(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerMuteRecordData record = itr.next();

        if (!localMuteStorage.isMuted(record.getUUID())) {
          continue;
        }

        localMuteStorage.unmute(localMuteStorage.getMute(record.getUUID()), record.getActor(plugin));

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }
  }
}
