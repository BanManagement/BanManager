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
          String dateTimeFormat;

          if (ban.getExpires() == 0) {
            kickMessage = Message.get("ban.player.kick");
            dateTimeFormat = Message.getString("ban.player.dateTimeFormat");
          } else {
            kickMessage = Message.get("tempban.player.kick");
            kickMessage.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
            dateTimeFormat = Message.getString("tempban.player.dateTimeFormat");
          }

          kickMessage
              .set("displayName", bukkitPlayer.getDisplayName())
              .set("player", ban.getName())
              .set("reason", ban.getReason())
              .set("actor", ban.getActor().getName())
              .set("created", DateUtils.format(dateTimeFormat != null ? dateTimeFormat : "yyyy-MM-dd HH:mm:ss", ban.getCreated()));

          bukkitPlayer.kick(kickMessage);
        });

      }
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to sync name bans", e);
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
      plugin.getLogger().warning("Failed to sync name bans", e);
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
