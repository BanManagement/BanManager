package me.confuser.banmanager.common.runnables;

import com.j256.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RollbackSync extends BmRunnable {

  public RollbackSync(BanManagerPlugin plugin) {
    super(plugin, "rollbacks");
  }

  @Override
  public void run() {
    CloseableIterator<RollbackData> itr = null;

    try {
      itr = plugin.getRollbackStorage().findRollbacks(lastChecked);

      while (itr.hasNext()) {
        final RollbackData data = itr.next();

        switch (data.getType()) {
          // @TODO Refactor/Clean up
          case "bans":
            for (Iterator<Map.Entry<UUID, PlayerBanData>> it = plugin.getPlayerBanStorage().getBans().entrySet()
                                                                     .iterator(); it.hasNext(); ) {
              Map.Entry<UUID, PlayerBanData> entry = it.next();

              if (!entry.getValue().getActor().getUUID().equals(data.getPlayer().getUUID())) continue;
              if (!(entry.getValue().getCreated() <= data.getCreated() && entry.getValue().getCreated() >= data
                      .getExpires())) continue;

              it.remove();
            }
            break;

          case "ipbans":
            for (Iterator<Map.Entry<Long, IpBanData>> it = plugin.getIpBanStorage().getBans().entrySet().iterator(); it
                    .hasNext(); ) {
              Map.Entry<Long, IpBanData> entry = it.next();

              if (!entry.getValue().getActor().getUUID().equals(data.getPlayer().getUUID())) continue;
              if (!(entry.getValue().getCreated() <= data.getCreated() && entry.getValue().getCreated() >= data
                      .getExpires())) continue;

              it.remove();
            }
            break;

          case "ipmutes":
            for (Iterator<Map.Entry<Long, IpMuteData>> it = plugin.getIpMuteStorage().getMutes().entrySet()
                                                                  .iterator(); it.hasNext(); ) {
              Map.Entry<Long, IpMuteData> entry = it.next();

              if (!entry.getValue().getActor().getUUID().equals(data.getPlayer().getUUID())) continue;
              if (!(entry.getValue().getCreated() <= data.getCreated() && entry.getValue().getCreated() >= data
                      .getExpires())) continue;

              it.remove();
            }
            break;

          case "mutes":
            for (Iterator<Map.Entry<UUID, PlayerMuteData>> it = plugin.getPlayerMuteStorage().getMutes().entrySet()
                                                                      .iterator(); it.hasNext(); ) {
              Map.Entry<UUID, PlayerMuteData> entry = it.next();

              if (!entry.getValue().getActor().getUUID().equals(data.getPlayer().getUUID())) continue;
              if (!(entry.getValue().getCreated() <= data.getCreated() && entry.getValue().getCreated() >= data
                      .getExpires())) continue;

              it.remove();
            }
            break;
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }
  }
}
