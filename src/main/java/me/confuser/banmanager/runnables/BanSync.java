package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerBanRecord;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BanSync extends BmRunnable {

  private PlayerBanStorage banStorage = plugin.getPlayerBanStorage();

  public BanSync() {
    super("playerBans");
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
  }

  private void newBans() {

    CloseableIterator<PlayerBanData> itr = null;
    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        final PlayerBanData ban = itr.next();

        if (banStorage.isBanned(ban.getPlayer().getUUID())) {
          if (ban.getUpdated() < lastChecked) continue;

          if (ban.equalsBan(banStorage.getBan(ban.getPlayer().getUUID()))) {
            continue;
          }
        }

        banStorage.addBan(ban);

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Player bukkitPlayer = CommandUtils.getPlayer(ban.getPlayer().getUUID());

            if (bukkitPlayer == null) return;

            Message kickMessage = Message.get("ban.player.kick")
                                         .set("displayName", bukkitPlayer.getDisplayName())
                                         .set("player", ban.getPlayer().getName())
                                         .set("reason", ban.getReason())
                                         .set("actor", ban.getActor().getName());

            bukkitPlayer.kickPlayer(kickMessage.toString());
          }
        });

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }

  private void newUnbans() {

    CloseableIterator<PlayerBanRecord> itr = null;
    try {
      itr = plugin.getPlayerBanRecordStorage().findUnbans(lastChecked);

      while (itr.hasNext()) {
        final PlayerBanRecord ban = itr.next();

        if (!banStorage.isBanned(ban.getPlayer().getUUID())) {
          continue;
        }

        if (!ban.equalsBan(banStorage.getBan(ban.getPlayer().getUUID()))) {
          continue;
        }

        banStorage.removeBan(ban.getPlayer().getUUID());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
