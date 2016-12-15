package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.data.NameBanData;
import me.confuser.banmanager.data.NameBanRecord;
import me.confuser.banmanager.storage.NameBanStorage;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class NameSync extends BmRunnable {

  private NameBanStorage banStorage = plugin.getNameBanStorage();

  public NameSync() {
    super("nameBans");
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
  }

  private void newBans() {

    CloseableIterator<NameBanData> itr = null;
    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        final NameBanData ban = itr.next();

        if (banStorage.isBanned(ban.getName()) && ban.getUpdated() < lastChecked) {
          continue;
        }

        banStorage.addBan(ban);

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Player bukkitPlayer = plugin.getServer().getPlayer(ban.getName());

            if (bukkitPlayer == null) return;

            Message kickMessage = Message.get("ban.player.kick")
                                         .set("displayName", bukkitPlayer.getDisplayName())
                                         .set("name", ban.getName())
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

    CloseableIterator<NameBanRecord> itr = null;
    try {
      itr = plugin.getNameBanRecordStorage().findUnbans(lastChecked);

      while (itr.hasNext()) {
        final NameBanRecord ban = itr.next();

        if (!banStorage.isBanned(ban.getName())) {
          continue;
        }

        if (!ban.equalsBan(banStorage.getBan(ban.getName()))) {
          continue;
        }

        banStorage.removeBan(ban.getName());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
