package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.storage.PlayerWarnStorage;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class WarningSync extends BmRunnable {

  private PlayerWarnStorage warnStorage;

  public WarningSync(BanManagerPlugin plugin) {
    super(plugin, "playerWarnings");

    warnStorage = plugin.getPlayerWarnStorage();
  }

  @Override
  public void run() {
    newWarnings();
  }

  private void newWarnings() {

    CloseableIterator<PlayerWarnData> itr = null;
    try {
      itr = warnStorage.findWarnings(lastChecked);

      while (itr.hasNext()) {
        final PlayerWarnData warn = itr.next();

        if (warn.isRead()) {
          continue;
        }

        plugin.getScheduler().runSync(() -> {
          CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(warn.getPlayer().getUUID());

          if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

          Message.get("warn.player.warned")
                 .set("displayName", bukkitPlayer.getDisplayName())
                 .set("player", warn.getPlayer().getName())
                 .set("reason", warn.getReason())
                 .set("actor", warn.getActor().getName())
                 .sendTo(bukkitPlayer);

          warn.setRead(true);

          plugin.getScheduler().runAsync(() -> {
            try {
              plugin.getPlayerWarnStorage().update(warn);
            } catch (SQLException e) {
              e.printStackTrace();
            }
          });

        });

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

}
