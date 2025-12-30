package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteRecordData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
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
  protected DatabaseConfig getCheckpointDbConfig() {
    return plugin.getConfig().getGlobalDb();
  }

  @Override
  protected BaseDaoImpl<?, ?> getCheckpointDao() {
    return muteStorage;
  }

  @Override
  public void run() {
    newMutes();
    newUnmutes();
  }

  private void newMutes() {
    CloseableIterator<GlobalPlayerMuteData> itr = null;
    try {
      itr = muteStorage.findMutes(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerMuteData mute = itr.next();

        PlayerMuteData localMute = localMuteStorage.retrieveMute(mute.getUUID());

        if (localMute != null) {
          if (localMute.equalsMute(mute.toLocal(plugin))) {
            continue;
          }

          // Global mute overrides local - respect event cancellation
          if (!localMuteStorage.unmute(localMute, mute.getActor(plugin))) {
            continue;
          }
        } else if (localMuteStorage.isMuted(mute.getUUID())) {
          localMuteStorage.removeMute(mute.getUUID());
        }

        if (!localMuteStorage.mute(mute.toLocal(plugin))) continue;

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

        PlayerMuteData localMute = localMuteStorage.getMute(record.getUUID());
        if (localMute == null) {
          continue;
        }

        localMuteStorage.unmute(localMute, record.getActor(plugin));

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null)
        itr.closeQuietly();
    }
  }
}
