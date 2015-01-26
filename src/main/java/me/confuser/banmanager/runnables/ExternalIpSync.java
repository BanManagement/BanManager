package me.confuser.banmanager.runnables;

import com.j256.ormlite.dao.CloseableIterator;
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

public class ExternalIpSync implements Runnable {

  private BanManager plugin = BanManager.getPlugin();
  private ExternalIpBanStorage banStorage = plugin.getExternalIpBanStorage();
  private IpBanStorage localBanStorage = plugin.getIpBanStorage();
  private ExternalIpBanRecordStorage recordStorage = plugin.getExternalIpBanRecordStorage();
  private long lastChecked = 0;

  public ExternalIpSync() {
    lastChecked = plugin.getSchedulesConfig().getLastChecked("externalIpBans");
  }

  @Override
  public void run() {
    // New/updated bans check
    try {
      newBans();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    // New unbans
    try {
      newUnbans();
    } catch (SQLException e) {
      e.printStackTrace();
    }

    lastChecked = System.currentTimeMillis() / 1000L;
    plugin.getSchedulesConfig().setLastChecked("externalIpBans", lastChecked);
  }


  private void newBans() throws SQLException {

    CloseableIterator<ExternalIpBanData> itr = banStorage.findBans(lastChecked);

    while (itr.hasNext()) {
      ExternalIpBanData ban = itr.next();

      final IpBanData localBan = ban.toLocal();

      if (localBanStorage.isBanned(ban.getIp())) {
        // External ban overrides local
        localBanStorage
                .unban(localBan, ban.getActor());
      }

      localBanStorage.ban(localBan);

      plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

        @Override
        public void run() {
          Message kickMessage = Message.get("ipBanKick").set("reason", localBan.getReason())
                                       .set("actor", localBan.getActor().getName());

          for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (IPUtils.toLong(onlinePlayer.getAddress().getAddress()) == localBan.getIp()) {
              onlinePlayer.kickPlayer(kickMessage.toString());
            }
          }
        }
      });

    }

    itr.close();
  }

  private void newUnbans() throws SQLException {

    CloseableIterator<ExternalIpBanRecordData> itr = recordStorage.findUnbans(lastChecked);

    while (itr.hasNext()) {
      ExternalIpBanRecordData record = itr.next();

      if (!localBanStorage.isBanned(record.getIp())) {
        continue;
      }

      localBanStorage.unban(localBanStorage.getBan(record.getIp()), record.getActor());

    }

    itr.close();
  }
}
