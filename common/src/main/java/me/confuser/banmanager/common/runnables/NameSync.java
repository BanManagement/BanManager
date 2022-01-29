package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.NameBanRecord;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.storage.NameBanStorage;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class NameSync extends BmRunnable {

  private NameBanStorage banStorage;

  public NameSync(BanManagerPlugin plugin) {
    super(plugin, "nameBans");

    banStorage = plugin.getNameBanStorage();
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

        plugin.getScheduler().runSync(() -> {
          CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(ban.getName());

          if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

          Message kickMessage;

          if (ban.getExpires() == 0) {
            kickMessage = Message.get("ban.player.kick");
          } else {
            kickMessage = Message.get("tempban.player.kick");
            kickMessage.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
          }

          kickMessage
              .set("displayName", bukkitPlayer.getDisplayName())
              .set("player", ban.getName())
              .set("reason", ban.getReason())
              .set("actor", ban.getActor().getName());

          bukkitPlayer.kick(kickMessage.toString());
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
