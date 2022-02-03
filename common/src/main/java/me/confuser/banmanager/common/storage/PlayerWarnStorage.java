package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.google.guava.cache.Cache;
import me.confuser.banmanager.common.google.guava.cache.CacheBuilder;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
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
import me.confuser.banmanager.common.util.UUIDUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerWarnStorage extends BaseDaoImpl<PlayerWarnData, Integer> {

  private BanManagerPlugin plugin;
  private Cache<UUID, PlayerWarnData> muteWarnings = CacheBuilder.newBuilder()
      .expireAfterWrite(1, TimeUnit.DAYS)
      .concurrencyLevel(2)
      .maximumSize(200)
      .build();

  public PlayerWarnStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerWarnData>) plugin.getConfig()
        .getLocalDb().getTable("playerWarnings"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig
            .getTableName() + " ADD COLUMN `expires` INT(10) NOT NULL DEFAULT 0," +
            " ADD KEY `" + tableConfig.getTableName() + "_expires_idx` (`expires`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
      try {
        String update = "ALTER TABLE " + tableConfig
            .getTableName() + " ADD COLUMN `points` INT(10) NOT NULL DEFAULT 1," +
            " ADD KEY `" + tableConfig.getTableName() + "_points_idx` (`points`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
      try {
        String update = "ALTER TABLE " + tableConfig
            .getTableName() + " MODIFY COLUMN `points` DECIMAL(60,2) NOT NULL DEFAULT 1";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }
  }

  public PlayerWarnStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerWarnData>) table);
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
    } catch (IOException e) {
      e.printStackTrace();
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

  public int deleteRecent(PlayerData player) throws SQLException {
    // TODO use a raw DELETE query to reduce to one query
    PlayerWarnData warning = queryBuilder().limit(1L).orderBy("created", false).where().eq("player_id", player)
        .queryForFirst();

    return delete(warning);
  }

  public void purge(CleanUp cleanup, boolean read) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(CURRENT_TIMESTAMP - INTERVAL '" + cleanup.getDays() + "' DAY)");
  }


  public CloseableIterator<PlayerWarnData> findWarnings(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<PlayerWarnData, Integer> query = queryBuilder();
    Where<PlayerWarnData, Integer> where = query.where();
    where.ge("created", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerWarnData, Integer> builder = deleteBuilder();

    builder.where().eq("player_id", player);

    return builder.delete();
  }
}
