package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.storage.PlayerBanStorage;
import me.confuser.banmanager.util.CommandUtils;

import java.sql.SQLException;

public class BanSync extends BmRunnable {

  private PlayerBanStorage banStorage = plugin.getPlayerBanStorage();

  public BanSync(BanManagerPlugin plugin) {
    super(plugin, "playerBans");
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

        plugin.getBootstrap().getScheduler().executeSync(() -> {


          Sender bukkitPlayer = CommandUtils.getSender(ban.getPlayer().getUUID());

          if (bukkitPlayer == null) return;

          String kickMessage = Message.BAN_PLAYER_KICK.asString(plugin.getLocaleManager(),
                                       "displayName", bukkitPlayer.getDisplayName(),
                                       "player", ban.getPlayer().getName(),
                                       "reason", ban.getReason(),
                                       "actor", ban.getActor().getName());

          bukkitPlayer.kick(kickMessage);
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
