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
import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.StorageUtils;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class IpBanStorage extends BaseDaoImpl<IpBanData, Integer> {

  private BanManagerPlugin plugin;
  @Getter
  private ConcurrentHashMap<String, IpBanData> bans = new ConcurrentHashMap<>();

  public IpBanStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<IpBanData>) plugin.getConfig().getLocalDb()
        .getTable("ipBans"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "ip");

      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `silent` TINYINT(1)");
      } catch (SQLException e) {
      }
    }

    loadAll();

    plugin.getLogger().info("Loaded " + bans.size() + " ip bans into memory");
  }

  public IpBanStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<IpBanData>) table);
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection(getTableName());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve ip bans into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, a.id, a.name, a.ip, a.lastSeen, t.ip, t.reason,");
    sql.append(" t.expires, t.created, t.updated, t.silent");
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

      plugin.getLogger().warning("Failed to retrieve ip bans into memory");
      return;
    }

    DatabaseResults results = null;

    try {
      results = statement.runQuery(null);

      while (results.next()) {
        PlayerData actor;
        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(1)), results.getString(2),
              IPUtils.toIPAddress(results.getBytes(3)),
              results.getLong(4));

        } catch (NullPointerException | AddressValueException e) {
          plugin.getLogger().warning("Missing or invalid actor for ip ban " + results.getInt(0) + ", ignored");
          continue;
        }

        IpBanData ban = new IpBanData(results.getInt(0), IPUtils.toIPAddress(results.getBytes(5)), actor, results.getString(6),
            results.getBoolean(10),
            results.getLong(7),
            results.getLong(8),
            results.getLong(9));

        bans.put(ban.getIp().toString(), ban);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      getConnectionSource().releaseConnection(connection);
    }
  }

  public boolean isBanned(IPAddress ip) {
    return bans.get(ip.toString()) != null;
  }

  public boolean isBanned(InetAddress address) {
    return isBanned(IPUtils.toIPAddress(address));
  }

  public IpBanData retrieveBan(IPAddress ip) throws SQLException {
    List<IpBanData> bans = queryForEq("ip", ip);

    if (bans.isEmpty()) return null;

    return bans.get(0);
  }

  public IpBanData getBan(IPAddress ip) {
    return bans.get(ip.toString());
  }

  public IpBanData getBan(InetAddress address) {
    return getBan(Objects.requireNonNull(IPUtils.toIPAddress(address)));
  }

  public void addBan(IpBanData ban) {
    bans.put(ban.getIp().toString(), ban);

    plugin.getServer().callEvent("IpBannedEvent", ban, ban.isSilent() || !plugin.getConfig().isBroadcastOnSync());
  }

  public void removeBan(IpBanData ban) {
    removeBan(ban.getIp());
  }

  public void removeBan(IPAddress ip) {
    bans.remove(ip.toString());
  }

  public boolean ban(IpBanData ban) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("IpBanEvent", ban, ban.isSilent());

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    bans.put(ban.getIp().toString(), ban);

    plugin.getServer().callEvent("IpBannedEvent", ban, event.isSilent());

    return true;
  }

  public boolean unban(IpBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(IpBanData ban, PlayerData actor, String reason) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("IpUnbanEvent", ban, actor, reason);

    if (event.isCancelled()) {
      return false;
    }

    delete(ban);
    bans.remove(ban.getIp().toString());

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

  public boolean isRecentlyBanned(IPAddress ip, long cooldown) throws SQLException {
    if (cooldown == 0) {
      return false;
    }

    return queryBuilder().where()
        .eq("ip", ip).and()
        .ge("created", (System.currentTimeMillis() / 1000L) - cooldown)
        .countOf() > 0;
  }
}
