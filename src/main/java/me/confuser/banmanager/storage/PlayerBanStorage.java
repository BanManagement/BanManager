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
import me.confuser.banmanager.events.PlayerBanEvent;
import me.confuser.banmanager.events.PlayerBannedEvent;
import me.confuser.banmanager.events.PlayerUnbanEvent;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerBanStorage extends BaseDaoImpl<PlayerBanData, Integer> {

  private BanManager plugin = BanManager.getPlugin();
  private ConcurrentHashMap<UUID, PlayerBanData> bans = new ConcurrentHashMap<>();

  public PlayerBanStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerBanData>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                                     .getTable("playerBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
      return;
    }

    loadAll();

    plugin.getLogger().info("Loaded " + bans.size() + " bans into memory");
  }

  private void loadAll(){
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection();
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve bans into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, p.id, p.name, p.ip, p.lastSeen, a.id, a.name, a.ip, a.lastSeen, t.reason,");
    sql.append(" t.expires, t.created, t.updated");
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
                  results.getLong(3),
                  results.getLong(4));
        } catch (NullPointerException e) {
          plugin.getLogger().warning("Missing player for ban " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerData actor;

        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(5)), results.getString(6),
                  results.getLong(7),
                  results.getLong(8));
        } catch (NullPointerException e) {
          plugin.getLogger().warning("Missing actor for ban " + results.getInt(0) + ", ignored");
          continue;
        }

        PlayerBanData ban = new PlayerBanData(results.getInt(0), player, actor, results.getString(9), results.getLong(10),
                results.getLong(11), results.getLong(12));

        bans.put(ban.getPlayer().getUUID(), ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      connection.closeQuietly();
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

    if (plugin.getConfiguration().isBroadcastOnSync()) {
      Bukkit.getServer().getPluginManager().callEvent(new PlayerBannedEvent(ban, false));
    }
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

  public boolean ban(PlayerBanData ban, boolean isSilent) throws SQLException {
    PlayerBanEvent event = new PlayerBanEvent(ban, isSilent);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    bans.put(ban.getPlayer().getUUID(), ban);

    Bukkit.getServer().getPluginManager().callEvent(new PlayerBannedEvent(ban, event.isSilent()));

    return true;
  }
  public boolean unban(PlayerBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(PlayerBanData ban, PlayerData actor, String reason) throws SQLException {
    return unban(ban, actor, reason, false);
  }

  public boolean unban(PlayerBanData ban, PlayerData actor, String reason, boolean delete) throws SQLException {
    PlayerUnbanEvent event = new PlayerUnbanEvent(ban, actor, reason);
    Bukkit.getServer().getPluginManager().callEvent(event);

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

  public List<PlayerData> getDuplicates(long ip) {
    ArrayList<PlayerData> players = new ArrayList<>();

    if (plugin.getConfiguration().getBypassPlayerIps().contains(ip)) {
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
}
