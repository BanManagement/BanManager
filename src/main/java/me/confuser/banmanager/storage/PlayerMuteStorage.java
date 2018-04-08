package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
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
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig
                .getTableName() + " ADD COLUMN `soft` TINYINT(1)," +
                " ADD KEY `" + tableConfig.getTableName() + "_soft_idx` (`soft`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
      try {
        String update = "ALTER TABLE " + tableConfig
                .getTableName() + " ADD UNIQUE KEY `" + tableConfig.getTableName() + "_player_idx` (`player_id`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }

    loadAll();

    plugin.getLogger().info("Loaded " + mutes.size() + " mutes into memory");
  }

  private void loadAll() {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve mutes into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, p.id, p.name, p.ip, p.lastSeen, a.id, a.name, a.ip, a.lastSeen, t.reason,");
    sql.append(" t.soft, t.expires, t.created, t.updated");
    sql.append(" FROM ");
    sql.append(this.getTableInfo().getTableName());
    sql.append(" t LEFT JOIN ");
    sql.append(plugin.getPlayerStorage().getTableInfo().getTableName());
    sql.append(" p ON player_id = p.id");
    sql.append(" LEFT JOIN ");
    sql.append(plugin.getPlayerStorage().getTableInfo().getTableName());
    sql.append(" a ON actor_id = a.id");

    CompiledStatement statement;

    try {
      statement = connection.compileStatement(sql.toString(), StatementBuilder.StatementType.SELECT, null,
              DatabaseConnection.DEFAULT_RESULT_FLAGS);
    } catch (SQLException e) {
      e.printStackTrace();
      connection.closeQuietly();

      plugin.getLogger().warning("Failed to retrieve mutes into memory");
      return;
    }

    DatabaseResults results = null;

    try {
      results = statement.runQuery(null);

      while (results.next()) {
        PlayerData player;

        try {
          player = new PlayerData(UUIDUtils.fromBytes(results.getBytes(1)), results.getString(2),
                  results.getLong(3),
                  results.getLong(4));
        } catch (NullPointerException e) {
          plugin.getLogger().warning("Missing player for mute " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerData actor;

        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(5)), results.getString(6),
                  results.getLong(7),
                  results.getLong(8));
        } catch (NullPointerException e) {
          plugin.getLogger().warning("Missing actor for mute " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerMuteData mute = new PlayerMuteData(results.getInt(0), player, actor, results.getString(9), results.getBoolean(10), results.getLong(11),
                results.getLong(12), results.getLong(13));

        mutes.put(mute.getPlayer().getUUID(), mute);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      connection.closeQuietly();
    }
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

    if (plugin.getConfiguration().isBroadcastOnSync()) {
      Bukkit.getServer().getPluginManager().callEvent(new PlayerMutedEvent(mute, false));
    }
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
    return unmute(mute, actor, "");
  }

  public boolean unmute(PlayerMuteData mute, PlayerData actor, String reason) throws SQLException {
    return unmute(mute, actor, reason, false);
  }

  public boolean unmute(PlayerMuteData mute, PlayerData actor, String reason, boolean delete) throws SQLException {
    PlayerUnmuteEvent event = new PlayerUnmuteEvent(mute, actor, reason);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    delete(mute);
    mutes.remove(mute.getPlayer().getUUID());

    if (!delete) plugin.getPlayerMuteRecordStorage().addRecord(mute, actor, reason);

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
