package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.banmanager.storage.PlayerWarnStorage;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class WarningSync extends BmRunnable {

  private PlayerWarnStorage warnStorage = plugin.getPlayerWarnStorage();

  public WarningSync() {
    super("playerWarnings");
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

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Player bukkitPlayer = plugin.getServer().getPlayer(warn.getPlayer().getUUID());

            if (bukkitPlayer == null) return;

            Message.get("warn.player.warned")
                   .set("displayName", bukkitPlayer.getPlayer().getDisplayName())
                   .set("player", warn.getPlayer().getName())
                   .set("reason", warn.getReason())
                   .set("actor", warn.getActor().getName())
                   .sendTo(bukkitPlayer);

            warn.setRead(true);

            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
              @Override
              public void run() {
                try {
                  plugin.getPlayerWarnStorage().update(warn);
                } catch (SQLException e) {
                  e.printStackTrace();
                }
              }
            });

          }
        });

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

}
