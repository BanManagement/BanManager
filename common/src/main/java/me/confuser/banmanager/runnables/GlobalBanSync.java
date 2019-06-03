package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.data.global.GlobalPlayerBanRecordData;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.banmanager.storage.global.GlobalPlayerBanRecordStorage;
import me.confuser.banmanager.storage.global.GlobalPlayerBanStorage;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class GlobalBanSync extends BmRunnable {

  private GlobalPlayerBanStorage banStorage = plugin.getGlobalPlayerBanStorage();
  private PlayerBanStorage localBanStorage = plugin.getPlayerBanStorage();
  private GlobalPlayerBanRecordStorage recordStorage = plugin.getGlobalPlayerBanRecordStorage();

  public GlobalBanSync(BanManagerPlugin plugin) {
    super(plugin,"externalPlayerBans");
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

        final PlayerBanData localBan = ban.toLocal();

        if (localBanStorage.retrieveBan(ban.getUUID()) != null) {
          // Global ban overrides local
          localBanStorage.unban(localBan, ban.getActor());
        } else if (localBanStorage.isBanned(ban.getUUID())) {
          localBanStorage.removeBan(ban.getUUID());
        }

        localBanStorage.ban(localBan, false);

        plugin.getBootstrap().getScheduler().executeSync(() -> {
          // TODO move into a listener
          Sender bukkitPlayer = CommandUtils.getSender(localBan.getPlayer().getUUID());

          if (bukkitPlayer == null) return;

          Message kickMessage;

          if (localBan.getExpires() == 0) {
            kickMessage = Message.BAN_PLAYER_KICK;
          } else {
            kickMessage = Message.TEMPBAN_PLAYER_KICK;
          }

          String message = kickMessage.asString(plugin.getLocaleManager(),
                  "displayName", bukkitPlayer.getDisplayName(),
                  "player", localBan.getPlayer().getName(),
                  "reason", localBan.getReason(),
                  "actor", localBan.getActor().getName(),
                  "expires", DateUtils.getDifferenceFormat(localBan.getExpires()));

          bukkitPlayer.kick(message);
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

        localBanStorage.unban(localBanStorage.getBan(record.getUUID()), record.getActor());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
