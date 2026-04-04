package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.configs.DatabaseConfig;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import me.confuser.banmanager.common.data.PlayerNameSummary;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.StatementBuilder;
import me.confuser.banmanager.common.ormlite.support.CompiledStatement;
import me.confuser.banmanager.common.ormlite.support.DatabaseConnection;
import me.confuser.banmanager.common.ormlite.support.DatabaseResults;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerHistoryStorage extends BaseDaoImpl<PlayerHistoryData, Integer> {

  private final ConcurrentHashMap<UUID, Integer> activeSessions = new ConcurrentHashMap<>();
  private final DatabaseConfig dbConfig;
  private final BanManagerPlugin plugin;

  public PlayerHistoryStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerHistoryData>) plugin.getConfig()
        .getLocalDb()
        .getTable("playerHistory"));

    this.plugin = plugin;
    this.dbConfig = plugin.getConfig().getLocalDb();

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  /**
   * Constructor for conversion/migration purposes only.
   * Does not support startSession/endSession methods.
   */
  public PlayerHistoryStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerHistoryData>) table);
    this.plugin = null;
    this.dbConfig = null;
  }

  /**
   * Start a new session for a player. Inserts a row using ORM, then updates timestamps to database time.
   * The session ID is stored in memory for later update on leave.
   *
   * @param player The player data
   * @param logIp Whether to record the IP address
   * @throws IllegalStateException if dbConfig is not set (conversion mode)
   */
  public void startSession(PlayerData player, boolean logIp) throws SQLException {
    if (dbConfig == null) {
      throw new IllegalStateException("startSession requires dbConfig to be set");
    }

    PlayerHistoryData session = new PlayerHistoryData(player, logIp);
    create(session);

    int sessionId = session.getId();
    String table = getTableInfo().getTableName();
    String nowExpr = dbConfig.getTimestampNow();

    updateRaw("UPDATE `" + table + "` SET `join` = " + nowExpr + ", `leave` = " + nowExpr + " WHERE `id` = ?",
        String.valueOf(sessionId));

    activeSessions.put(player.getUUID(), sessionId);
  }

  /**
   * End a session for a player. Updates the leave time to the current database time.
   * Note: For async usage, prefer removeSession() + endSessionById() to avoid race conditions.
   *
   * @param uuid The player's UUID
   * @return true if the session was found and updated, false otherwise
   * @throws IllegalStateException if dbConfig is not set (conversion mode)
   */
  public boolean endSession(UUID uuid) throws SQLException {
    Integer sessionId = activeSessions.remove(uuid);
    if (sessionId == null) {
      return false;
    }
    return endSessionById(sessionId);
  }

  /**
   * End a session by its database ID. Updates the leave time to the current database time.
   * Use this when you've already retrieved the session ID (e.g., for async processing).
   *
   * @param sessionId The session database ID
   * @return true if the update was successful
   * @throws IllegalStateException if dbConfig is not set (conversion mode)
   */
  public boolean endSessionById(int sessionId) throws SQLException {
    if (dbConfig == null) {
      throw new IllegalStateException("endSessionById requires dbConfig to be set");
    }

    String table = getTableInfo().getTableName();
    String nowExpr = dbConfig.getTimestampNow();

    updateRaw("UPDATE `" + table + "` SET `leave` = " + nowExpr + " WHERE `id` = ?",
        String.valueOf(sessionId));
    return true;
  }

  /**
   * Remove a session from memory without updating the database.
   * Used when you need to handle the session manually.
   *
   * @param uuid The player's UUID
   * @return The session ID if found, null otherwise
   */
  public Integer removeSession(UUID uuid) {
    return activeSessions.remove(uuid);
  }

  /**
   * Check if a player has an active session.
   */
  public boolean hasActiveSession(UUID uuid) {
    return activeSessions.containsKey(uuid);
  }

  public CloseableIterator<PlayerHistoryData> getSince(PlayerData player, long since, int page) throws SQLException {
    return queryBuilder().limit(10L).offset(10L * page)
        .orderBy("join", false)
        .where().ge("join", since).and().eq("player_id", player)
        .iterator();
  }

  /**
   * Get a summary of distinct names with aggregated first/last seen times.
   * firstSeen = MIN(join) across all sessions for that name
   * lastSeen = MAX(leave) across all sessions for that name
   *
   * @param player The player
   * @return List of PlayerNameSummary ordered by lastSeen DESC
   * @throws SQLException if database error occurs
   */
  public List<PlayerNameSummary> getNamesSummary(PlayerData player) throws SQLException {
    String table = getTableInfo().getTableName();
    String sql = "SELECT `name`, MIN(`join`) AS firstSeen, MAX(`leave`) AS lastSeen"
        + " FROM `" + table + "`"
        + " WHERE `player_id` = ? AND `name` IS NOT NULL AND `name` != ''"
        + " GROUP BY `name` ORDER BY lastSeen DESC";

    List<PlayerNameSummary> summaries = new ArrayList<>();

    try (DatabaseConnection connection = connectionSource.getReadOnlyConnection(table)) {
      CompiledStatement statement = connection.compileStatement(
          sql, StatementBuilder.StatementType.SELECT, null,
          DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
      try {
        statement.setObject(0, player.getId(), SqlType.BYTE_ARRAY);
        DatabaseResults results = statement.runQuery(null);
        try {
          while (results.next()) {
            summaries.add(new PlayerNameSummary(
                results.getString(0),
                results.getLong(1),
                results.getLong(2)));
          }
        } finally {
          try { results.close(); } catch (IOException ignored) { }
        }
      } finally {
        try { statement.close(); } catch (IOException ignored) { }
      }
    } catch (IOException e) {
      throw new SQLException("Failed to query name summary", e);
    }

    return summaries;
  }

  /**
   * Get the name a player was using at a specific timestamp.
   * Finds the session where join <= ts AND leave >= ts.
   *
   * @param player The player
   * @param timestamp The timestamp to check
   * @return The name at that time, or null if not found
   * @throws SQLException if database error occurs
   */
  public String getNameAt(PlayerData player, long timestamp) throws SQLException {
    List<PlayerHistoryData> results = queryBuilder()
        .limit(1L)
        .orderBy("join", false)
        .where()
        .eq("player_id", player)
        .and()
        .le("join", timestamp)
        .and()
        .ge("leave", timestamp)
        .query();

    return results.isEmpty() ? null : results.get(0).getName();
  }

  /**
   * End all active sessions. Called on shutdown.
   * Updates leave time to database time for all remaining sessions.
   */
  public void save() {
    if (dbConfig == null || activeSessions.isEmpty()) {
      return;
    }

    String table = getTableInfo().getTableName();
    String nowExpr = dbConfig.getTimestampNow();

    List<Integer> ids = new ArrayList<>(activeSessions.values());

    for (int i = 0; i < ids.size(); i += 500) {
      List<Integer> batch = ids.subList(i, Math.min(i + 500, ids.size()));
      String csv = batch.stream().map(String::valueOf).collect(Collectors.joining(","));
      try {
        updateRaw("UPDATE `" + table + "` SET `leave` = " + nowExpr
            + " WHERE `id` IN (" + csv + ")");
      } catch (SQLException e) {
        plugin.getLogger().warning("Failed to process player history operation", e);
        break;
      }
    }

    activeSessions.clear();
  }

  /**
   * Purge old session history records.
   * Only purges records where:
   * - ip IS NOT NULL (has IP) AND ip is not currently banned
   * - OR ip IS NULL (name-only records, always eligible for purge by age)
   * And leave timestamp is older than the configured days.
   */
  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    String table = getTableInfo().getTableName();
    String banTable = BanManagerPlugin.getInstance().getIpBanStorage()
        .getTableInfo()
        .getTableName();

    String sql = "DELETE FROM `" + table + "` WHERE `id` IN ("
        + "SELECT ph.id FROM (SELECT p.id FROM `" + table + "` p "
        + "LEFT JOIN `" + banTable + "` b ON p.ip = b.ip "
        + "WHERE (p.ip IS NULL OR b.ip IS NULL) "
        + "AND p.`leave` < UNIX_TIMESTAMP(CURRENT_TIMESTAMP - INTERVAL "
        + cleanup.getDays() + " DAY)) AS ph)";

    updateRaw(sql);
  }
}
