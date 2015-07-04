package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.external.ExternalPlayerBanData;
import me.confuser.banmanager.data.external.ExternalPlayerBanRecordData;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.banmanager.storage.external.ExternalPlayerBanRecordStorage;
import me.confuser.banmanager.storage.external.ExternalPlayerBanStorage;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ExternalBanSync implements Runnable {

  private BanManager plugin = BanManager.getPlugin();
  private ExternalPlayerBanStorage banStorage = plugin.getExternalPlayerBanStorage();
  private PlayerBanStorage localBanStorage = plugin.getPlayerBanStorage();
  private ExternalPlayerBanRecordStorage recordStorage = plugin.getExternalPlayerBanRecordStorage();
  private long lastChecked = 0;
  @Getter
  private boolean isRunning = false;

  public ExternalBanSync() {
    lastChecked = plugin.getSchedulesConfig().getLastChecked("externalPlayerBans");
  }

  @Override
  public void run() {
    if (isRunning) return;

    isRunning = true;
    // New/updated bans check
    newBans();

    // New unbans
    newUnbans();

    lastChecked = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastChecked("externalPlayerBans", lastChecked);
    isRunning = false;
  }


  private void newBans() {

    CloseableIterator<ExternalPlayerBanData> itr = null;
    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        ExternalPlayerBanData ban = itr.next();

        final PlayerBanData localBan = ban.toLocal();

        if (localBanStorage.retrieveBan(ban.getUUID()) != null) {
          // External ban overrides local
          localBanStorage
                  .unban(localBan, ban.getActor());
        } else if (localBanStorage.isBanned(ban.getUUID())) {
          localBanStorage.removeBan(ban.getUUID());
        }

        localBanStorage.ban(localBan, false);

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            // TODO move into a listener
            Player bukkitPlayer = plugin.getServer().getPlayer(localBan.getPlayer().getUUID());

            if (bukkitPlayer == null) return;

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

    CloseableIterator<ExternalPlayerBanRecordData> itr = null;
    try {
      itr = recordStorage.findUnbans(lastChecked);

      while (itr.hasNext()) {
        ExternalPlayerBanRecordData record = itr.next();

        if (!localBanStorage.isBanned(record.getUUID())) {
          continue;
        }

        localBanStorage.unban(localBanStorage.getBan(record.getUUID()), record.getActor());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
