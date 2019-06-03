package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.data.PlayerWarnData;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.storage.PlayerWarnStorage;
import me.confuser.banmanager.util.CommandUtils;

import java.sql.SQLException;

public class WarningSync extends BmRunnable {

  private PlayerWarnStorage warnStorage = plugin.getPlayerWarnStorage();

  public WarningSync(BanManagerPlugin plugin) {
    super(plugin,"playerWarnings");
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

        plugin.getBootstrap().getScheduler().executeSync(() -> {
          Sender bukkitPlayer = CommandUtils.getSender(warn.getPlayer().getUUID());

          if (bukkitPlayer == null) return;

          Message.WARN_PLAYER_WARNED.send(bukkitPlayer,
                 "displayName", bukkitPlayer.getDisplayName(),
                 "player", warn.getPlayer().getName(),
                 "reason", warn.getReason(),
                 "actor", warn.getActor().getName());

          warn.setRead(true);

          plugin.getBootstrap().getScheduler().executeAsync(() -> {
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
