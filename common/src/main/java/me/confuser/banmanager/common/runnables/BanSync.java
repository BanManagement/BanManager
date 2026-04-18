package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerBanRecord;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.storage.PlayerBanStorage;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class BanSync extends BmRunnable {

  private PlayerBanStorage banStorage;

  public BanSync(BanManagerPlugin plugin) {
    super(plugin, "playerBans");

    banStorage = plugin.getPlayerBanStorage();
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

        plugin.getScheduler().runSync(() -> {
          CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(ban.getPlayer().getUUID());

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
              .set("player", ban.getPlayer().getName())
              .set("reason", ban.getReason())
              .set("actor", ban.getActor().getName())
              .set("created", DateUtils.format(dateTimeFormat != null ? dateTimeFormat : "yyyy-MM-dd HH:mm:ss", ban.getCreated()));

          bukkitPlayer.kick(kickMessage);
        });

      }
    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to sync bans", e);
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
      plugin.getLogger().warning("Failed to sync bans", e);
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
