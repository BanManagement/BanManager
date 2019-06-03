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
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.data.NameBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.events.NameBanEvent;
import me.confuser.banmanager.events.NameBannedEvent;
import me.confuser.banmanager.events.NameUnbanEvent;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class NameBanStorage extends BaseDaoImpl<NameBanData, Integer> {

  private BanManagerPlugin plugin = BanManager.getPlugin();
  private ConcurrentHashMap<String, NameBanData> bans = new ConcurrentHashMap<>();

  public NameBanStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<NameBanData>) BanManager.getPlugin().getLocalDb()
                                                                   .getTable("nameBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
      return;
    }

    loadAll();

    plugin.getLogger().info("Loaded " + bans.size() + " name bans into memory");
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection(getTableName());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warn("Failed to retrieve name bans into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, t.name, a.id, a.name, a.ip, a.lastSeen, t.reason,");
    sql.append(" t.expires, t.created, t.updated");
    sql.append(" FROM ");
    sql.append(this.getTableInfo().getTableName());
    sql.append(" t LEFT JOIN ");
    sql.append(plugin.getPlayerStorage().getTableInfo().getTableName());
    sql.append(" a ON actor_id = a.id");

    CompiledStatement statement;

    try {
      statement = connection.compileStatement(sql.toString(), StatementBuilder.StatementType.SELECT, null,
              DatabaseConnection.DEFAULT_RESULT_FLAGS, false);
    } catch (SQLException e) {
      e.printStackTrace();
      getConnectionSource().releaseConnection(connection);

      plugin.getLogger().warn("Failed to retrieve name bans into memory");
      return;
    }

    DatabaseResults results = null;

    try {
      results = statement.runQuery(null);

      while (results.next()) {
        PlayerData actor;

        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(2)), results.getString(3),
                  results.getLong(4),
                  results.getLong(5));
        } catch (NullPointerException e) {
          plugin.getLogger().warn("Missing actor for ban " + results.getInt(0) + ", ignored");
          continue;
        }

        NameBanData ban = new NameBanData(results.getInt(0), results.getString(1), actor, results.getString(6), results
                .getLong(7), results.getLong(8), results.getLong(9));

        bans.put(ban.getName().toLowerCase(), ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      getConnectionSource().releaseConnection(connection);
    }
  }

  public ConcurrentHashMap<String, NameBanData> getBans() {
    return bans;
  }

  public boolean isBanned(String name) {
    return bans.get(name.toLowerCase()) != null;
  }

  public void addBan(NameBanData ban) {
    bans.put(ban.getName().toLowerCase(), ban);
  }

  public void removeBan(NameBanData ban) {
    removeBan(ban.getName());
  }

  public void removeBan(String name) {
    bans.remove(name.toLowerCase());
  }

  public NameBanData getBan(String playerName) {
    return bans.get(playerName.toLowerCase());
  }

  public boolean ban(NameBanData ban, boolean isSilent) throws SQLException {
    NameBanEvent event = new NameBanEvent(ban, isSilent);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    bans.put(ban.getName().toLowerCase(), ban);

    Bukkit.getServer().getPluginManager().callEvent(new NameBannedEvent(ban, event.isSilent()));

    return true;
  }

  public boolean unban(NameBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(NameBanData ban, PlayerData actor, String reason) throws SQLException {
    return unban(ban, actor, reason, false);
  }

  public boolean unban(NameBanData ban, PlayerData actor, String reason, boolean delete) throws SQLException {
    NameUnbanEvent event = new NameUnbanEvent(ban, actor, reason);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    delete(ban);
    bans.remove(ban.getName().toLowerCase());

    if (!delete) plugin.getNameBanRecordStorage().addRecord(ban, actor, reason);

    return true;
  }

  public CloseableIterator<NameBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<NameBanData, Integer> query = queryBuilder();
    Where<NameBanData, Integer> where = query.where();
    where
            .ge("created", checkTime)
            .or()
            .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }
}
