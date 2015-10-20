package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import lombok.Getter;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.external.ExternalIpBanData;
import me.confuser.banmanager.data.external.ExternalIpBanRecordData;
import me.confuser.banmanager.storage.IpBanStorage;
import me.confuser.banmanager.storage.external.ExternalIpBanRecordStorage;
import me.confuser.banmanager.storage.external.ExternalIpBanStorage;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class ExternalIpSync extends BmRunnable {

  private ExternalIpBanStorage banStorage = plugin.getExternalIpBanStorage();
  private IpBanStorage localBanStorage = plugin.getIpBanStorage();
  private ExternalIpBanRecordStorage recordStorage = plugin.getExternalIpBanRecordStorage();

  public ExternalIpSync() {
    super("externalIpBans");
  }

  @Override
  public void run() {
    newBans();
    newUnbans();
  }


  private void newBans() {

    CloseableIterator<ExternalIpBanData> itr = null;
    try {
      itr = banStorage.findBans(lastChecked);

      while (itr.hasNext()) {
        ExternalIpBanData ban = itr.next();

        final IpBanData localBan = ban.toLocal();

        if (localBanStorage.retrieveBan(ban.getIp()) != null) {
          // External ban overrides local
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

    CloseableIterator<ExternalIpBanRecordData> itr = null;
    try {
      itr = recordStorage.findUnbans(lastChecked);

      while (itr.hasNext()) {
        ExternalIpBanRecordData record = itr.next();

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
