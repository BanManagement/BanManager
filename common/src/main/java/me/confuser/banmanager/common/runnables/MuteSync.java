package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerMuteRecord;
import me.confuser.banmanager.common.storage.PlayerMuteStorage;

import java.sql.SQLException;

public class MuteSync extends BmRunnable {

  private PlayerMuteStorage muteStorage;

  public MuteSync(BanManagerPlugin plugin) {
    super(plugin, "playerMutes");

    muteStorage = plugin.getPlayerMuteStorage();
  }

  @Override
  public void run() {
    newMutes();
    newUnmutes();
  }

  private void newMutes() {

    CloseableIterator<PlayerMuteData> itr = null;
    try {
      itr = muteStorage.findMutes(lastChecked);

      while (itr.hasNext()) {
        final PlayerMuteData mute = itr.next();

        if (muteStorage.isMuted(mute.getPlayer().getUUID())) {
          if (mute.getUpdated() < lastChecked) continue;

          if (mute.equalsMute(muteStorage.getMute(mute.getPlayer().getUUID()))) {
            continue;
          }
        }

        muteStorage.addMute(mute);

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  private void newUnmutes() {

    CloseableIterator<PlayerMuteRecord> itr = null;
    try {
      itr = plugin.getPlayerMuteRecordStorage().findUnmutes(lastChecked);

      while (itr.hasNext()) {
        final PlayerMuteRecord mute = itr.next();

        if (!muteStorage.isMuted(mute.getPlayer().getUUID())) {
          continue;
        }

        if (!mute.equalsMute(muteStorage.getMute(mute.getPlayer().getUUID()))) {
          continue;
        }

        muteStorage.removeMute(mute.getPlayer().getUUID());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
