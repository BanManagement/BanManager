package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.global.GlobalIpBanData;
import me.confuser.banmanager.data.global.GlobalIpBanRecordData;
import me.confuser.banmanager.storage.IpBanStorage;
import me.confuser.banmanager.storage.global.GlobalIpBanRecordStorage;
import me.confuser.banmanager.storage.global.GlobalIpBanStorage;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class GlobalIpSync extends BmRunnable {

  private GlobalIpBanStorage banStorage = plugin.getGlobalIpBanStorage();
  private IpBanStorage localBanStorage = plugin.getIpBanStorage();
  private GlobalIpBanRecordStorage recordStorage = plugin.getGlobalIpBanRecordStorage();

  public GlobalIpSync() {
    super("externalIpBans");
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
          localBanStorage
                  .unban(localBan, ban.getActor());
        } else if (localBanStorage.isBanned(ban.getIp())) {
          localBanStorage.removeBan(ban.getIp());
        }

        localBanStorage.ban(localBan, false);

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Message kickMessage = Message.get("banip.ip.kick").set("reason", localBan.getReason())
                                         .set("actor", localBan.getActor().getName());

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
              if (IPUtils.toLong(onlinePlayer.getAddress().getAddress()) == localBan.getIp()) {
                onlinePlayer.kickPlayer(kickMessage.toString());
              }
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

        localBanStorage.unban(localBanStorage.getBan(record.getIp()), record.getActor());

      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

  }
}
