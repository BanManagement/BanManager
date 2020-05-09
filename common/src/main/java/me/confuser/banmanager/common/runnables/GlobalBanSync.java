package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanRecordData;
import me.confuser.banmanager.common.storage.PlayerBanStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerBanRecordStorage;
import me.confuser.banmanager.common.storage.global.GlobalPlayerBanStorage;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class GlobalBanSync extends BmRunnable {

  private GlobalPlayerBanStorage banStorage;
  private PlayerBanStorage localBanStorage;
  private GlobalPlayerBanRecordStorage recordStorage;

  public GlobalBanSync(BanManagerPlugin plugin) {
    super(plugin, "externalPlayerBans");

    banStorage = plugin.getGlobalPlayerBanStorage();
    localBanStorage = plugin.getPlayerBanStorage();
    recordStorage = plugin.getGlobalPlayerBanRecordStorage();
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
  }

  private void newBans() {
    CloseableIterator<GlobalPlayerBanData> itr = null;

    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerBanData ban = itr.next();

        final PlayerBanData localBan = localBanStorage.retrieveBan(ban.getUUID());

        if (localBan != null) {
          // Global ban overrides local
          localBanStorage
                  .unban(localBan, ban.getActor(plugin));
        } else if (localBanStorage.isBanned(ban.getUUID())) {
          localBanStorage.removeBan(ban.getUUID());
        }

        localBanStorage.ban(ban.toLocal(plugin));

        plugin.getScheduler().runSync(() -> {
          // TODO move into a listener
          CommonPlayer bukkitPlayer = plugin.getServer().getPlayer(localBan.getPlayer().getUUID());

          if (bukkitPlayer == null || !bukkitPlayer.isOnline()) return;

          Message kickMessage;

          if (localBan.getExpires() == 0) {
            kickMessage = Message.get("ban.player.kick");
          } else {
            kickMessage = Message.get("tempban.player.kick");
            kickMessage.set("expires", DateUtils.getDifferenceFormat(localBan.getExpires()));
          }

          kickMessage
                  .set("displayName", bukkitPlayer.getDisplayName())
                  .set("player", localBan.getPlayer().getName())
                  .set("reason", localBan.getReason())
                  .set("actor", localBan.getActor().getName());

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
    CloseableIterator<GlobalPlayerBanRecordData> itr = null;

    try {
      itr = recordStorage.findUnbans(lastChecked);

      while (itr.hasNext()) {
        GlobalPlayerBanRecordData record = itr.next();

        if (!localBanStorage.isBanned(record.getUUID())) {
          continue;
        }

        localBanStorage.unban(localBanStorage.getBan(record.getUUID()), record.getActor(plugin));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
