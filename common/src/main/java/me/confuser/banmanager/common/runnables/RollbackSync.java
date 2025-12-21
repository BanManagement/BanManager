package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;

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
            for (Iterator<Map.Entry<String, IpBanData>> it = plugin.getIpBanStorage().getBans().entrySet().iterator(); it
                .hasNext(); ) {
              Map.Entry<String, IpBanData> entry = it.next();

              if (!entry.getValue().getActor().getUUID().equals(data.getPlayer().getUUID())) continue;
              if (!(entry.getValue().getCreated() <= data.getCreated() && entry.getValue().getCreated() >= data
                  .getExpires())) continue;

              it.remove();
            }
            break;

          case "ipmutes":
            for (Iterator<Map.Entry<String, IpMuteData>> it = plugin.getIpMuteStorage().getMutes().entrySet()
                .iterator(); it.hasNext(); ) {
              Map.Entry<String, IpMuteData> entry = it.next();

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

          case "banrecords":
            // Sync restored bans to in-memory cache
            // Query for bans that were restored (created within rollback timeframe)
            CloseableIterator<PlayerBanData> restoredBans = plugin.getPlayerBanStorage()
                .queryBuilder()
                .where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator();

            try {
              while (restoredBans.hasNext()) {
                PlayerBanData ban = restoredBans.next();
                if (!plugin.getPlayerBanStorage().isBanned(ban.getPlayer().getUUID())) {
                  plugin.getPlayerBanStorage().addBan(ban);
                }
              }
            } finally {
              restoredBans.closeQuietly();
            }
            break;

          case "ipbanrecords":
            // Sync restored IP bans to in-memory cache
            CloseableIterator<IpBanData> restoredIpBans = plugin.getIpBanStorage()
                .queryBuilder()
                .where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator();

            try {
              while (restoredIpBans.hasNext()) {
                IpBanData ban = restoredIpBans.next();
                if (!plugin.getIpBanStorage().isBanned(ban.getIp())) {
                  plugin.getIpBanStorage().addBan(ban);
                }
              }
            } finally {
              restoredIpBans.closeQuietly();
            }
            break;

          case "muterecords":
            // Sync restored mutes to in-memory cache
            CloseableIterator<PlayerMuteData> restoredMutes = plugin.getPlayerMuteStorage()
                .queryBuilder()
                .where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator();

            try {
              while (restoredMutes.hasNext()) {
                PlayerMuteData mute = restoredMutes.next();
                if (!plugin.getPlayerMuteStorage().isMuted(mute.getPlayer().getUUID())) {
                  plugin.getPlayerMuteStorage().addMute(mute);
                }
              }
            } finally {
              restoredMutes.closeQuietly();
            }
            break;

          case "ipmuterecords":
            // Sync restored IP mutes to in-memory cache
            CloseableIterator<IpMuteData> restoredIpMutes = plugin.getIpMuteStorage()
                .queryBuilder()
                .where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator();

            try {
              while (restoredIpMutes.hasNext()) {
                IpMuteData mute = restoredIpMutes.next();
                if (!plugin.getIpMuteStorage().isMuted(mute.getIp())) {
                  plugin.getIpMuteStorage().addMute(mute);
                }
              }
            } finally {
              restoredIpMutes.closeQuietly();
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
