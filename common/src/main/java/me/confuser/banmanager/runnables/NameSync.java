package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.data.NameBanData;
import me.confuser.banmanager.data.NameBanRecord;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.storage.NameBanStorage;

import java.sql.SQLException;

public class NameSync extends BmRunnable {

  private NameBanStorage banStorage = plugin.getNameBanStorage();

  public NameSync(BanManagerPlugin plugin) {
    super(plugin,"nameBans");
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

        plugin.getBootstrap().getScheduler().executeSync(() -> {

          Sender bukkitPlayer = plugin.getBootstrap().getPlayerAsSender(ban.getName()).orElse(null);

          if (bukkitPlayer == null) return;

          String kickMessage = Message.BAN_PLAYER_KICK.asString(plugin.getLocaleManager(),
                                       "displayName", bukkitPlayer.getDisplayName(),
                                       "name", ban.getName(),
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
