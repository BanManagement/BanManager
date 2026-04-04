package me.confuser.banmanager.common.runnables;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;

import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
          case "bans":
            evictFromCache(plugin.getPlayerBanStorage().getBans(),
                v -> v.getActor().getUUID(), PlayerBanData::getCreated, data);
            break;

          case "ipbans":
            evictFromCache(plugin.getIpBanStorage().getBans(),
                v -> v.getActor().getUUID(), IpBanData::getCreated, data);
            break;

          case "ipmutes":
            evictFromCache(plugin.getIpMuteStorage().getMutes(),
                v -> v.getActor().getUUID(), IpMuteData::getCreated, data);
            break;

          case "mutes":
            evictFromCache(plugin.getPlayerMuteStorage().getMutes(),
                v -> v.getActor().getUUID(), PlayerMuteData::getCreated, data);
            break;

          case "banrecords":
            restoreToCache(plugin.getPlayerBanStorage()
                .queryBuilder().where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator(),
                ban -> !plugin.getPlayerBanStorage().isBanned(ban.getPlayer().getUUID()),
                ban -> plugin.getPlayerBanStorage().addBan(ban));
            break;

          case "ipbanrecords":
            restoreToCache(plugin.getIpBanStorage()
                .queryBuilder().where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator(),
                ban -> !plugin.getIpBanStorage().isBanned(ban.getIp()),
                ban -> plugin.getIpBanStorage().addBan(ban));
            break;

          case "muterecords":
            restoreToCache(plugin.getPlayerMuteStorage()
                .queryBuilder().where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator(),
                mute -> !plugin.getPlayerMuteStorage().isMuted(mute.getPlayer().getUUID()),
                mute -> plugin.getPlayerMuteStorage().addMute(mute));
            break;

          case "ipmuterecords":
            restoreToCache(plugin.getIpMuteStorage()
                .queryBuilder().where()
                .le("created", data.getCreated())
                .and().ge("created", data.getExpires())
                .iterator(),
                mute -> !plugin.getIpMuteStorage().isMuted(mute.getIp()),
                mute -> plugin.getIpMuteStorage().addMute(mute));
            break;
        }
      }

    } catch (SQLException e) {
      plugin.getLogger().warning("Failed to sync rollbacks", e);
    } finally {
      if (itr != null) itr.closeQuietly();
    }
  }

  private <K, V> void evictFromCache(Map<K, V> cache, Function<V, UUID> getActor,
      Function<V, Long> getCreated, RollbackData data) {
    UUID actorUUID = data.getPlayer().getUUID();
    long created = data.getCreated();
    long expires = data.getExpires();

    cache.entrySet().removeIf(entry -> {
      V value = entry.getValue();
      long entryCreated = getCreated.apply(value);
      return getActor.apply(value).equals(actorUUID)
          && entryCreated <= created
          && entryCreated >= expires;
    });
  }

  private <T> void restoreToCache(CloseableIterator<T> itr,
      Predicate<T> shouldRestore, Consumer<T> action) {
    try {
      while (itr.hasNext()) {
        T item = itr.next();
        if (shouldRestore.test(item)) {
          action.accept(item);
        }
      }
    } finally {
      itr.closeQuietly();
    }
  }
}
