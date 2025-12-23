package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.ipaddr.AddressValueException;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.TransactionHelper;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMuteStorage extends BaseDaoImpl<PlayerMuteData, Integer> {

  private BanManagerPlugin plugin;
  private ConcurrentHashMap<UUID, PlayerMuteData> mutes = new ConcurrentHashMap<>();

  public PlayerMuteStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerMuteData>) plugin.getConfig()
        .getLocalDb().getTable("playerMutes"));
    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
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
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `silent` TINYINT(1)");
      } catch (SQLException e) {
      }

      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `created` `created` BIGINT UNSIGNED,"
          + " CHANGE `updated` `updated` BIGINT UNSIGNED,"
          + " CHANGE `expires` `expires` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }
    }

    loadAll();

    plugin.getLogger().info("Loaded " + mutes.size() + " mutes into memory");
  }

  public PlayerMuteStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerMuteData>) table);
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection(getTableName());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve mutes into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, p.id, p.name, p.ip, p.lastSeen, a.id, a.name, a.ip, a.lastSeen, t.reason,");
    sql.append(" t.soft, t.expires, t.created, t.updated, t.silent");
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
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
    } catch (SQLException e) {
      e.printStackTrace();
      getConnectionSource().releaseConnection(connection);

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
              IPUtils.toIPAddress(results.getBytes(3)),
              results.getLong(4));
        } catch (NullPointerException | AddressValueException e) {
          plugin.getLogger().warning("Missing or invalid player for mute " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerData actor;

        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(5)), results.getString(6),
              IPUtils.toIPAddress(results.getBytes(7)),
              results.getLong(8));
        } catch (NullPointerException | AddressValueException e) {
          plugin.getLogger().warning("Missing or invalid actor for mute " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerMuteData mute = new PlayerMuteData(results.getInt(0), player, actor,
            results.getString(9),
            results.getBoolean(14),
            results.getBoolean(10),
            results.getLong(11),
            results.getLong(12),
            results.getLong(13));

        mutes.put(mute.getPlayer().getUUID(), mute);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      getConnectionSource().releaseConnection(connection);
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

    plugin.getServer().callEvent("PlayerMutedEvent", mute, mute.isSilent() || !plugin.getConfig().isBroadcastOnSync());
  }

  public boolean mute(PlayerMuteData mute) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("PlayerMuteEvent", mute, mute.isSilent());

    if (event.isCancelled()) {
      return false;
    }

    create(mute);
    mutes.put(mute.getPlayer().getUUID(), mute);

    plugin.getServer().callEvent("PlayerMutedEvent", mute, event.isSilent());

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
    CommonEvent event = plugin.getServer().callEvent("PlayerUnmuteEvent", mute, actor, reason);

    if (event.isCancelled()) {
      return false;
    }

    TransactionHelper.runInTransaction(connectionSource, () -> {
      delete(mute);
      if (!delete) plugin.getPlayerMuteRecordStorage().addRecord(mute, actor, reason);
    });

    mutes.remove(mute.getPlayer().getUUID());

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

  public boolean isRecentlyMuted(PlayerData player, long cooldown) throws SQLException {
    if (cooldown == 0) {
      return false;
    }

    return queryBuilder().where()
        .eq("player_id", player).and()
        .ge("created", (System.currentTimeMillis() / 1000L) - cooldown)
        .countOf() > 0;
  }
}
