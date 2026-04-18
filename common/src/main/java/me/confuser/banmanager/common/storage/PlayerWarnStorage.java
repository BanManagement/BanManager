package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.google.guava.cache.Cache;
import me.confuser.banmanager.common.google.guava.cache.CacheBuilder;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.stmt.UpdateBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.StorageUtils;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerWarnStorage extends BaseStorage<PlayerWarnData, Integer> {

  private Cache<UUID, PlayerWarnData> muteWarnings = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.DAYS)
      .concurrencyLevel(2)
      .maximumSize(200)
      .build();

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public PlayerWarnStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<PlayerWarnData>) plugin.getConfig()
        .getLocalDb().getTable("playerWarnings"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public PlayerWarnStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<PlayerWarnData>) table, plugin.getConfig().getLocalDb());
  }

  public boolean isMuted(UUID uuid) {
    return getMute(uuid) != null;
  }

  public PlayerWarnData getMute(UUID uuid) {
    return muteWarnings.getIfPresent(uuid);
  }

  public PlayerWarnData removeMute(UUID uuid) {
    PlayerWarnData warning = muteWarnings.getIfPresent(uuid);

    muteWarnings.invalidate(uuid);

    return warning;
  }

  public boolean addWarning(PlayerWarnData data, boolean silent) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("PlayerWarnEvent", data, silent);

    if (event.isCancelled()) {
      return false;
    }

    if (plugin.getConfig().isWarningMutesEnabled()) muteWarnings.put(data.getPlayer().getUUID(), data);

    boolean created = create(data) == 1;

    if (created) plugin.getServer().callEvent("PlayerWarnedEvent", data, event.isSilent());

    return created;
  }

  public CloseableIterator<PlayerWarnData> getUnreadWarnings(UUID uniqueId) throws SQLException {
    return queryBuilder().where().eq("player_id", UUIDUtils.toBytes(uniqueId)).and().eq("read", false).iterator();
  }

  public CloseableIterator<PlayerWarnData> getWarnings(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).iterator();
  }

  public long getCount(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).countOf();
  }

  public double getPointsCount(PlayerData player) throws SQLException {
    try (DatabaseConnection connection = connectionSource.getReadOnlyConnection(getTableName())) {
      CompiledStatement statement = connection
          .compileStatement("SELECT SUM(points) AS points FROM " + getTableName() + " WHERE player_id = ?", StatementBuilder.StatementType.SELECT, null, DatabaseConnection
              .DEFAULT_RESULT_FLAGS, false);

      statement.setObject(0, player.getId(), SqlType.BYTE_ARRAY);

      DatabaseResults results = statement.runQuery(null);

      if (results.next()) return results.getDouble(0);
    } catch (SQLException e) {
      throw e;
    } catch (Exception e) {
      plugin.getLogger().warning("Failed to process player warn operation", e);
    }

    return 0;
  }

  public double getPointsCount(PlayerData player, long timeframe) throws SQLException {
    try (DatabaseConnection connection = connectionSource.getReadOnlyConnection(getTableName())) {
      CompiledStatement statement = connection
          .compileStatement("SELECT SUM(points) AS points FROM " + getTableName() + " WHERE player_id = ? AND created >= ?", StatementBuilder.StatementType.SELECT, null, DatabaseConnection
              .DEFAULT_RESULT_FLAGS, false);

      statement.setObject(0, player.getId(), SqlType.BYTE_ARRAY);
      statement.setObject(1, timeframe, SqlType.LONG);

      DatabaseResults results = statement.runQuery(null);

      if (results.next()) return results.getDouble(0);
    } catch (SQLException e) {
      throw e;
    } catch (Exception e) {
      plugin.getLogger().warning("Failed to process player warn operation", e);
    }

    return 0;
  }

  public boolean isRecentlyWarned(PlayerData player, long cooldown) throws SQLException {
    if (cooldown == 0) {
      return false;
    }

    return queryBuilder().where()
        .eq("player_id", player).and()
        .ge("created", (System.currentTimeMillis() / 1000L) - cooldown)
        .countOf() > 0;
  }

  public int markAllRead(UUID playerId) throws SQLException {
    UpdateBuilder<PlayerWarnData, Integer> builder = updateBuilder();
    builder.updateColumnValue("read", true);
    builder.where().eq("player_id", UUIDUtils.toBytes(playerId)).and().eq("read", false);
    return builder.update();
  }

  public int deleteRecent(PlayerData player) throws SQLException {
    String table = getTableName();

    try (DatabaseConnection connection = connectionSource.getReadWriteConnection(table)) {
      CompiledStatement statement = connection.compileStatement(
          "DELETE FROM `" + table + "` WHERE `id` = ("
              + "SELECT `id` FROM (SELECT `id` FROM `" + table
              + "` WHERE `player_id` = ? ORDER BY `created` DESC LIMIT 1) AS tmp)",
          StatementBuilder.StatementType.UPDATE, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
      try {
        statement.setObject(0, player.getId(), SqlType.BYTE_ARRAY);
        return statement.runUpdate();
      } finally {
        try { statement.close(); } catch (Exception ignored) { }
      }
    } catch (Exception e) {
      throw StorageUtils.toSqlException("Failed to delete recent warning", e);
    }
  }

  public void purge(CleanUp cleanup, boolean read) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(CURRENT_TIMESTAMP - INTERVAL '" + cleanup.getDays() + "' DAY)");
  }


  public CloseableIterator<PlayerWarnData> findWarnings(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<PlayerWarnData, Integer> query = queryBuilder();
    Where<PlayerWarnData, Integer> where = query.where();
    where.ge("created", fromTime);

    query.setWhere(where);

    return query.iterator();

  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerWarnData, Integer> builder = deleteBuilder();

    builder.where().eq("player_id", player);

    return builder.delete();
  }
}
