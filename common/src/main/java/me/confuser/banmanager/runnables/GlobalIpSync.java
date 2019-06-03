package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.global.GlobalIpBanData;
import me.confuser.banmanager.data.global.GlobalIpBanRecordData;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.storage.IpBanStorage;
import me.confuser.banmanager.storage.global.GlobalIpBanRecordStorage;
import me.confuser.banmanager.storage.global.GlobalIpBanStorage;
import me.confuser.banmanager.util.IPUtils;

import java.sql.SQLException;
import java.util.UUID;
import java.util.stream.Collectors;

public class GlobalIpSync extends BmRunnable {

  private GlobalIpBanStorage banStorage = plugin.getGlobalIpBanStorage();
  private IpBanStorage localBanStorage = plugin.getIpBanStorage();
  private GlobalIpBanRecordStorage recordStorage = plugin.getGlobalIpBanRecordStorage();

  public GlobalIpSync(BanManagerPlugin plugin) {
    super(plugin,"externalIpBans");
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

        final IpBanData localBan = ban.toLocal();

        if (localBanStorage.retrieveBan(ban.getIp()) != null) {
          // Global ban overrides local
          localBanStorage.unban(localBan, ban.getActor());
        } else if (localBanStorage.isBanned(ban.getIp())) {
          localBanStorage.removeBan(ban.getIp());
        }

        localBanStorage.ban(localBan, false);

        plugin.getBootstrap().getScheduler().executeSync(() -> {
          String kickMessage = Message.BANIP_IP_KICK.asString(plugin.getLocaleManager(), "reason", localBan.getReason(), "actor", localBan.getActor().getName());

          for (UUID onlineUUID : plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList())) {
            plugin.getBootstrap().getPlayerAsSender(onlineUUID).ifPresent(target -> {
              if(IPUtils.toLong(target.getIPAddress()) == localBan.getIp()) {
                target.kick(kickMessage);
              }
            });
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

        localBanStorage.unban(localBanStorage.getBan(record.getIp()), record.getActor());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
