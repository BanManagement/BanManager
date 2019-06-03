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
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.events.IpBanEvent;
import me.confuser.banmanager.events.IpBannedEvent;
import me.confuser.banmanager.events.IpUnbanEvent;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IpBanStorage extends BaseDaoImpl<IpBanData, Integer> {

  private BanManagerPlugin plugin = BanManager.getPlugin();
  private ConcurrentHashMap<Long, IpBanData> bans = new ConcurrentHashMap<>();

  public IpBanStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<IpBanData>) BanManager.getPlugin().getLocalDb()
                                                                 .getTable("ipBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }

    loadAll();

    plugin.getLogger().info("Loaded " + bans.size() + " ip bans into memory");
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection(getTableName());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warn("Failed to retrieve ip bans into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, a.id, a.name, a.ip, a.lastSeen, t.ip, t.reason,");
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

      plugin.getLogger().warn("Failed to retrieve ip bans into memory");
      return;
    }

    DatabaseResults results = null;

    try {
      results = statement.runQuery(null);

      while (results.next()) {
        PlayerData actor;
        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(1)), results.getString(2),
                  results.getLong(3),
                  results.getLong(4));

        } catch (NullPointerException e) {
          plugin.getLogger().warn("Missing actor for ip ban " + results.getInt(0) + ", ignored");
          continue;
        }

        IpBanData ban = new IpBanData(results.getInt(0), results.getLong(5), actor, results.getString(6),
                results.getLong(7),
                results.getLong(8),
                results.getLong(9));

        bans.put(ban.getIp(), ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      getConnectionSource().releaseConnection(connection);
    }
  }

  public ConcurrentHashMap<Long, IpBanData> getBans() {
    return bans;
  }

  public boolean isBanned(long ip) {
    return bans.get(ip) != null;
  }

  public boolean isBanned(InetAddress address) {
    return isBanned(IPUtils.toLong(address));
  }

  public IpBanData retrieveBan(long ip) throws SQLException {
    List<IpBanData> bans = queryForEq("ip", ip);

    if (bans.isEmpty()) return null;

    return bans.get(0);
  }

  public IpBanData getBan(long ip) {
    return bans.get(ip);
  }

  public IpBanData getBan(InetAddress address) {
    return getBan(IPUtils.toLong(address));
  }

  public void addBan(IpBanData ban) {
    bans.put(ban.getIp(), ban);

    if (plugin.getConfiguration().isBroadcastOnSync()) {
      Bukkit.getServer().getPluginManager().callEvent(new IpBannedEvent(ban, false));
    }
  }

  public void removeBan(IpBanData ban) {
    removeBan(ban.getIp());
  }

  public void removeBan(long ip) {
    bans.remove(ip);
  }

  public boolean ban(IpBanData ban, boolean isSilent) throws SQLException {
    IpBanEvent event = new IpBanEvent(ban, isSilent);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    bans.put(ban.getIp(), ban);

    Bukkit.getServer().getPluginManager().callEvent(new IpBannedEvent(ban, event.isSilent()));

    return true;
  }

  public boolean unban(IpBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(IpBanData ban, PlayerData actor, String reason) throws SQLException {
    IpUnbanEvent event = new IpUnbanEvent(ban, actor, reason);
    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    delete(ban);
    bans.remove(ban.getIp());

    plugin.getIpBanRecordStorage().addRecord(ban, actor, reason);

    return true;
  }

  public CloseableIterator<IpBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpBanData, Integer> query = queryBuilder();
    Where<IpBanData, Integer> where = query.where();
    where
            .ge("created", checkTime)
            .or()
            .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

}
