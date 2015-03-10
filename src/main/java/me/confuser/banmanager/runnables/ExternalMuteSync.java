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
    isRunning = true;
    // New/updated mutes check
    try {
      newMutes();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // New unmutes
    try {
      newUnmutes();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    lastChecked = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastChecked("externalPlayerMutes", lastChecked);
    isRunning = false;
  }

  private void newMutes() throws SQLException {

    CloseableIterator<ExternalPlayerMuteData> itr = muteStorage.findMutes(lastChecked);

    while (itr.hasNext()) {
      ExternalPlayerMuteData mute = itr.next();

      final PlayerMuteData localMute = mute.toLocal();

      if (localMuteStorage.isMuted(mute.getUUID())) {
        // External mute overrides local
        localMuteStorage
                .unmute(localMute, mute.getActor());
      }

      localMuteStorage.mute(localMute);

    }

    itr.close();
  }

  private void newUnmutes() throws SQLException {

    CloseableIterator<ExternalPlayerMuteRecordData> itr = recordStorage.findUnmutes(lastChecked);

    while (itr.hasNext()) {
      ExternalPlayerMuteRecordData record = itr.next();

      if (!localMuteStorage.isMuted(record.getUUID())) {
        continue;
      }

      localMuteStorage.unmute(localMuteStorage.getMute(record.getUUID()), record.getActor());

    }
  }
}
