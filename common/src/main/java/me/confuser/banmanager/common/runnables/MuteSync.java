package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerMuteRecord;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
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

        PlayerMuteData existingMute = muteStorage.getMute(mute.getPlayer().getUUID());

        if (existingMute != null) {
          if (mute.getUpdated() < lastChecked) continue;

          if (mute.equalsMute(existingMute)) {
            continue;
          }

          // Mute exists but has changed - check if it's just a pause/resume state change
          // If core mute properties match but pause/resume state differs, update silently
          if (isSameMuteWithStateChange(mute, existingMute)) {
            muteStorage.updateMuteState(mute);
          } else {
            // Core mute changed (e.g., reason, actor, original duration) - fire event
            muteStorage.addMute(mute);
          }
        } else {
          // New mute - fire event
          muteStorage.addMute(mute);
        }

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  /**
   * Checks if two mutes represent the same mute with only pause/resume state changes.
   * Returns true if the core mute properties match (player, actor, reason, created, onlineOnly)
   * but the state fields differ (expires, pausedRemaining, updated).
   */
  private boolean isSameMuteWithStateChange(PlayerMuteData newMute, PlayerMuteData existingMute) {
    return newMute.getPlayer().getUUID().equals(existingMute.getPlayer().getUUID())
        && newMute.getActor().getUUID().equals(existingMute.getActor().getUUID())
        && newMute.getReason().equals(existingMute.getReason())
        && newMute.getCreated() == existingMute.getCreated()
        && newMute.isOnlineOnly() == existingMute.isOnlineOnly()
        && newMute.isSilent() == existingMute.isSilent()
        && newMute.isSoft() == existingMute.isSoft();
  }

  private void newUnmutes() {

    CloseableIterator<PlayerMuteRecord> itr = null;
    try {
      itr = plugin.getPlayerMuteRecordStorage().findUnmutes(lastChecked);

      while (itr.hasNext()) {
        final PlayerMuteRecord record = itr.next();

        PlayerMuteData localMute = muteStorage.getMute(record.getPlayer().getUUID());
        if (localMute == null) {
          continue;
        }

        if (!record.equalsMute(localMute)) {
          continue;
        }

        muteStorage.removeMute(record.getPlayer().getUUID());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
