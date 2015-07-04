package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.external.ExternalPlayerMuteData;
import me.confuser.banmanager.data.external.ExternalPlayerMuteRecordData;
import me.confuser.banmanager.storage.PlayerMuteStorage;
import me.confuser.banmanager.storage.external.ExternalPlayerMuteRecordStorage;
import me.confuser.banmanager.storage.external.ExternalPlayerMuteStorage;

import java.sql.SQLException;

public class ExternalMuteSync implements Runnable {

  private BanManager plugin = BanManager.getPlugin();
  private ExternalPlayerMuteStorage muteStorage = plugin.getExternalPlayerMuteStorage();
  private PlayerMuteStorage localMuteStorage = plugin.getPlayerMuteStorage();
  private ExternalPlayerMuteRecordStorage recordStorage = plugin.getExternalPlayerMuteRecordStorage();
  private long lastChecked = 0;
  @Getter
  private boolean isRunning = false;

  public ExternalMuteSync() {
    lastChecked = plugin.getSchedulesConfig().getLastChecked("externalPlayerMutes");
  }

  @Override
  public void run() {
    if (isRunning) return;

    isRunning = true;
    // New/updated mutes check
    newMutes();

    // New unmutes
    newUnmutes();

    lastChecked = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastChecked("externalPlayerMutes", lastChecked);
    isRunning = false;
  }

  private void newMutes() {

    CloseableIterator<ExternalPlayerMuteData> itr = null;
    try {
      itr = muteStorage.findMutes(lastChecked);

      while (itr.hasNext()) {
        ExternalPlayerMuteData mute = itr.next();

        final PlayerMuteData localMute = mute.toLocal();

        if (localMuteStorage.retrieveMute(mute.getUUID()) != null) {
          // External mute overrides local
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

    CloseableIterator<ExternalPlayerMuteRecordData> itr = null;
    try {
      itr = recordStorage.findUnmutes(lastChecked);

      while (itr.hasNext()) {
        ExternalPlayerMuteRecordData record = itr.next();

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
