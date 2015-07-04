package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.events.PlayerMuteEvent;
import me.confuser.banmanager.events.PlayerMutedEvent;
import me.confuser.banmanager.events.PlayerUnmuteEvent;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMuteStorage extends BaseDaoImpl<PlayerMuteData, Integer> {

  private BanManager plugin = BanManager.getPlugin();
  private ConcurrentHashMap<UUID, PlayerMuteData> mutes = new ConcurrentHashMap<>();

  public PlayerMuteStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerMuteData>) BanManager.getPlugin().getConfiguration()
                                                                      .getLocalDb().getTable("playerMutes"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }

    CloseableIterator<PlayerMuteData> itr = iterator();

    while (itr.hasNext()) {
      PlayerMuteData mute = itr.next();

      mutes.put(mute.getPlayer().getUUID(), mute);
    }

    itr.close();

    plugin.getLogger().info("Loaded " + mutes.size() + " mutes into memory");
  }

  public ConcurrentHashMap<UUID, PlayerMuteData> getMutes() {
    return mutes;
  }

  public boolean isMuted(UUID uuid) {
    return mutes.get(uuid) != null;
  }

  public PlayerMuteData retrieveMute(UUID uuid) throws SQLException {
    List<PlayerMuteData> mutes = queryForEq("player_id", UUIDUtils.toBytes(uuid));

    if (mutes.isEmpty()) return null;

    return mutes.get(0);
  }

  public boolean isMuted(String playerName) {
    for (PlayerMuteData mute : mutes.values()) {
      if (mute.getPlayer().getName().equalsIgnoreCase(playerName)) {
        return true;
      }
    }

    return false;
  }

  public PlayerMuteData getMute(UUID uuid) {
    return mutes.get(uuid);
  }

  public PlayerMuteData getMute(String playerName) {
    for (PlayerMuteData mute : mutes.values()) {
      if (mute.getPlayer().getName().equalsIgnoreCase(playerName)) {
        return mute;
      }
    }

    return null;
  }

  public void addMute(PlayerMuteData mute) {
    mutes.put(mute.getPlayer().getUUID(), mute);
  }

  public boolean mute(PlayerMuteData mute, boolean silent) throws SQLException {
    PlayerMuteEvent event = new PlayerMuteEvent(mute, silent);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    create(mute);
    mutes.put(mute.getPlayer().getUUID(), mute);

    Bukkit.getServer().getPluginManager().callEvent(new PlayerMutedEvent(mute, event.isSilent()));

    return true;
  }

  public void removeMute(UUID uuid) {
    mutes.remove(uuid);
  }

  public void removeMute(PlayerMuteData mute) {
    removeMute(mute.getPlayer().getUUID());
  }

  public boolean unmute(PlayerMuteData mute, PlayerData actor) throws SQLException {
    PlayerUnmuteEvent event = new PlayerUnmuteEvent(mute);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    delete(mute);
    mutes.remove(mute.getPlayer().getUUID());

    plugin.getPlayerMuteRecordStorage().addRecord(mute, actor);

    return true;
  }

  public CloseableIterator<PlayerMuteData> findMutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<PlayerMuteData, Integer> query = queryBuilder();
    Where<PlayerMuteData, Integer> where = query.where();
    where
            .ge("created", checkTime)
            .or()
            .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();
  }
}
