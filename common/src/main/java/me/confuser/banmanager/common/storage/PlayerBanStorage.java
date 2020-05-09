package me.confuser.banmanager.common.storage;

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
import inet.ipaddr.AddressValueException;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBanStorage extends BaseDaoImpl<PlayerBanData, Integer> {

  private BanManagerPlugin plugin;
  private ConcurrentHashMap<UUID, PlayerBanData> bans = new ConcurrentHashMap<>();

  public PlayerBanStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerBanData>) plugin.getConfig().getLocalDb()
        .getTable("playerBans"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
      return;
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `silent` TINYINT(1)");
      } catch (SQLException e) {
      }
    }

    loadAll();

    plugin.getLogger().info("Loaded " + bans.size() + " bans into memory");
  }

  public PlayerBanStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerBanData>) table);
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection(getTableName());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve bans into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, p.id, p.name, p.ip, p.lastSeen, a.id, a.name, a.ip, a.lastSeen, t.reason,");
    sql.append(" t.expires, t.created, t.updated, t.silent");
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
      this.getConnectionSource().releaseConnection(connection);

      plugin.getLogger().warning("Failed to retrieve bans into memory");
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
          plugin.getLogger().warning("Missing or invalid player for ban " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerData actor;

        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(5)), results.getString(6),
              IPUtils.toIPAddress(results.getBytes(7)),
              results.getLong(8));
        } catch (NullPointerException | AddressValueException e) {
          plugin.getLogger().warning("Missing or invalid actor for ban " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerBanData ban = new PlayerBanData(results.getInt(0), player, actor,
            results.getString(9),
            results.getBoolean(13),
            results.getLong(10),
            results.getLong(11),
            results.getLong(12));

        bans.put(ban.getPlayer().getUUID(), ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      this.getConnectionSource().releaseConnection(connection);
    }
  }

  public ConcurrentHashMap<UUID, PlayerBanData> getBans() {
    return bans;
  }

  public boolean isBanned(UUID uuid) {
    return bans.get(uuid) != null;
  }

  public boolean isBanned(String playerName) {
    return getBan(playerName) != null;
  }

  public PlayerBanData retrieveBan(UUID uuid) throws SQLException {
    List<PlayerBanData> bans = queryForEq("player_id", UUIDUtils.toBytes(uuid));

    if (bans.isEmpty()) return null;

    return bans.get(0);
  }

  public PlayerBanData getBan(UUID uuid) {
    return bans.get(uuid);
  }

  public void addBan(PlayerBanData ban) {
    bans.put(ban.getPlayer().getUUID(), ban);

    plugin.getServer().callEvent("PlayerBannedEvent", ban, ban.isSilent() || !plugin.getConfig().isBroadcastOnSync());
  }

  public void removeBan(PlayerBanData ban) {
    removeBan(ban.getPlayer().getUUID());
  }

  public void removeBan(UUID uuid) {
    bans.remove(uuid);
  }

  public PlayerBanData getBan(String playerName) {
    for (PlayerBanData ban : bans.values()) {
      if (ban.getPlayer().getName().equalsIgnoreCase(playerName)) {
        return ban;
      }
    }

    return null;
  }

  public boolean ban(PlayerBanData ban) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("PlayerBanEvent", ban, ban.isSilent());

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    bans.put(ban.getPlayer().getUUID(), ban);

    plugin.getServer().callEvent("PlayerBannedEvent", ban, event.isSilent());

    return true;
  }

  public boolean unban(PlayerBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(PlayerBanData ban, PlayerData actor, String reason) throws SQLException {
    return unban(ban, actor, reason, false);
  }

  public boolean unban(PlayerBanData ban, PlayerData actor, String reason, boolean delete) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("PlayerUnbanEvent", ban, actor, reason);

    if (event.isCancelled()) {
      return false;
    }

    delete(ban);
    bans.remove(ban.getPlayer().getUUID());

    if (!delete) plugin.getPlayerBanRecordStorage().addRecord(ban, actor, reason);

    return true;
  }

  public CloseableIterator<PlayerBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<PlayerBanData, Integer> query = queryBuilder();
    Where<PlayerBanData, Integer> where = query.where();
    where
        .ge("created", checkTime)
        .or()
        .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

  public List<PlayerData> getDuplicates(IPAddress ip) {
    ArrayList<PlayerData> players = new ArrayList<>();

    if (plugin.getConfig().getBypassPlayerIps().contains(ip.toString())) {
      return players;
    }

    QueryBuilder<PlayerBanData, Integer> query = queryBuilder();
    try {
      QueryBuilder<PlayerData, byte[]> playerQuery = plugin.getPlayerStorage().queryBuilder();

      Where<PlayerData, byte[]> where = playerQuery.where();
      where.eq("ip", ip);
      playerQuery.setWhere(where);

      query.leftJoin(playerQuery);
    } catch (SQLException e) {
      e.printStackTrace();
      return players;
    }

    CloseableIterator<PlayerBanData> itr = null;
    try {
      itr = query.iterator();

      while (itr.hasNext()) {
        players.add(itr.next().getPlayer());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (itr != null) itr.closeQuietly();
    }

    return players;
  }

  public boolean isRecentlyBanned(PlayerData player, long cooldown) throws SQLException {
    if (cooldown == 0) {
      return false;
    }

    return queryBuilder().where()
        .eq("player_id", player).and()
        .ge("created", (System.currentTimeMillis() / 1000L) - cooldown)
        .countOf() > 0;
  }
}
