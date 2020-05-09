package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.global.GlobalIpBanData;
import me.confuser.banmanager.common.data.global.GlobalIpBanRecordData;
import me.confuser.banmanager.common.storage.IpBanStorage;
import me.confuser.banmanager.common.storage.global.GlobalIpBanRecordStorage;
import me.confuser.banmanager.common.storage.global.GlobalIpBanStorage;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class GlobalIpSync extends BmRunnable {

  private GlobalIpBanStorage banStorage;
  private IpBanStorage localBanStorage;
  private GlobalIpBanRecordStorage recordStorage;

  public GlobalIpSync(BanManagerPlugin plugin) {
    super(plugin, "externalIpBans");

    banStorage = plugin.getGlobalIpBanStorage();
    localBanStorage = plugin.getIpBanStorage();
    recordStorage = plugin.getGlobalIpBanRecordStorage();
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
  }


  private void newBans() {

    CloseableIterator<GlobalIpBanData> itr = null;
    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        GlobalIpBanData ban = itr.next();

        final IpBanData localBan = localBanStorage.retrieveBan(ban.getIp());

        if (localBan != null) {
          // Global ban overrides local
          localBanStorage
              .unban(localBan, ban.getActor(plugin));
        } else if (localBanStorage.isBanned(ban.getIp())) {
          localBanStorage.removeBan(ban.getIp());
        }

        localBanStorage.ban(ban.toLocal(plugin));

        plugin.getScheduler().runSync(() -> {
          Message kickMessage = Message.get("banip.ip.kick").set("reason", localBan.getReason())
              .set("actor", localBan.getActor().getName());

          for (CommonPlayer onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (IPUtils.toIPAddress(onlinePlayer.getAddress()).equals(localBan.getIp())) {
              onlinePlayer.kick(kickMessage.toString());
            }
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

    CloseableIterator<GlobalIpBanRecordData> itr = null;
    try {
      itr = recordStorage.findUnbans(lastChecked);

      while (itr.hasNext()) {
        GlobalIpBanRecordData record = itr.next();

        if (!localBanStorage.isBanned(record.getIp())) {
          continue;
        }

        localBanStorage.unban(localBanStorage.getBan(record.getIp()), record.getActor(plugin));

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
