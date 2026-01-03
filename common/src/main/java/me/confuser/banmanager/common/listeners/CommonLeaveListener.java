package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerMuteData;

import java.sql.SQLException;
import java.util.UUID;

public class CommonLeaveListener {
  private BanManagerPlugin plugin;

  public CommonLeaveListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  public void onLeave(UUID id, String name) {
    final Integer sessionId = plugin.getPlayerHistoryStorage().removeSession(id);

    if (sessionId == null) {
      plugin.getLogger().warning("Could not find " + name + " session history, perhaps they " +
          "disconnected too quickly?");
    } else {
      plugin.getScheduler().runAsync(() -> {
        try {
          plugin.getPlayerHistoryStorage().endSessionById(sessionId);
        } catch (SQLException e) {
          e.printStackTrace();
        }
      });
    }

    PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(id);
    if (mute != null && mute.isOnlineOnly() && !mute.isPaused() && mute.getExpires() > 0) {
      long now = System.currentTimeMillis() / 1000L;
      long remaining = mute.getExpires() - now;

      if (remaining > 0) {
        plugin.getScheduler().runAsync(() -> {
          try {
            plugin.getPlayerMuteStorage().pauseMute(mute, remaining);
          } catch (SQLException e) {
            e.printStackTrace();
          }
        });
      }
    }
  }
}
