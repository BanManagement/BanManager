package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.google.guava.collect.Range;
import me.confuser.banmanager.common.google.guava.collect.TreeRangeSet;
import me.confuser.banmanager.common.ipaddr.AddressValueException;
import me.confuser.banmanager.common.ipaddr.IPAddress;
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
import me.confuser.banmanager.common.util.StorageUtils;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class IpRangeBanStorage extends BaseDaoImpl<IpRangeBanData, Integer> {

  private BanManagerPlugin plugin;
  private TreeRangeSet<IPAddress> ranges = TreeRangeSet.create();
  private ConcurrentHashMap<Range<IPAddress>, IpRangeBanData> bans = new ConcurrentHashMap<>();

  public IpRangeBanStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<IpRangeBanData>) plugin.getConfig().getLocalDb()
        .getTable("ipRangeBans"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
      return;
    } else {
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "fromIp");
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "toIp");

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

    plugin.getLogger().info("Loaded " + bans.size() + " ip range bans into memory");
  }

  public IpRangeBanStorage(ConnectionSource connection, DatabaseTableConfig<?> ipRangeBans) throws SQLException {
    super(connection, (DatabaseTableConfig<IpRangeBanData>) ipRangeBans);
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection(getTableName());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve ip range bans into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, a.id, a.name, a.ip, a.lastSeen, t.fromIp, t.toIp, t.reason,");
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

      plugin.getLogger().warning("Failed to retrieve ip range bans into memory");
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

        IpRangeBanData ban = new IpRangeBanData(results.getInt(0),
            IPUtils.toIPAddress(results.getBytes(5)),
            IPUtils.toIPAddress(results.getBytes(6)),
            actor,
            results.getString(7),
            results.getBoolean(11),
            results.getLong(8),
            results.getLong(9),
            results.getLong(10));

        Range<IPAddress> range = Range.closed(ban.getFromIp(), ban.getToIp());

        bans.put(range, ban);
        ranges.add(range);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      getConnectionSource().releaseConnection(connection);
    }
  }

  public ConcurrentHashMap<Range<IPAddress>, IpRangeBanData> getBans() {
    return bans;
  }

  public boolean isBanned(IPAddress ip) {
    return ranges.contains(ip);
  }

  private Range getRange(IPAddress ip) {
    return ranges.rangeContaining(ip);
  }

  public boolean isBanned(Range range) {
    return bans.get(range) != null;
  }

  public boolean isBanned(IpRangeBanData ban) {
    return isBanned(ban.getRange());
  }

  public boolean isBanned(InetAddress address) {
    return isBanned(IPUtils.toIPAddress(address));
  }

  public IpRangeBanData retrieveBan(IPAddress fromIp, IPAddress toIp) throws SQLException {
    QueryBuilder<IpRangeBanData, Integer> query = this.queryBuilder();
    Where<IpRangeBanData, Integer> where = queryBuilder().where();

    where.eq("fromIp", fromIp).eq("toIp", toIp);

    query.setWhere(where);

    return query.queryForFirst();
  }

  public IpRangeBanData getBan(IPAddress ip) {
    Range range = getRange(ip);

    if (range == null) return null;

    return bans.get(range);
  }

  public IpRangeBanData getBan(Range range) {
    return bans.get(range);
  }

  public IpRangeBanData getBan(InetAddress address) {
    return getBan(IPUtils.toIPAddress(address));
  }

  public void addBan(IpRangeBanData ban) {
    Range range = Range.closed(ban.getFromIp(), ban.getToIp());

    ranges.add(range);
    bans.put(range, ban);

    plugin.getServer().callEvent("IpRangeBannedEvent", ban, ban.isSilent() || !plugin.getConfig().isBroadcastOnSync());
  }

  public void removeBan(IpRangeBanData ban) {
    removeBan(ban.getRange());
  }

  public void removeBan(Range range) {
    ranges.remove(range);
    bans.remove(range);
  }

  public boolean ban(IpRangeBanData ban) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("IpRangeBanEvent", ban, ban.isSilent());

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    Range range = Range.closed(ban.getFromIp(), ban.getToIp());

    bans.put(range, ban);
    ranges.add(range);

    plugin.getServer().callEvent("IpRangeBannedEvent", ban, event.isSilent());

    return true;
  }

  public boolean unban(IpRangeBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(IpRangeBanData ban, PlayerData actor, String reason) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("IpRangeUnbanEvent", ban, actor, reason);

    if (event.isCancelled()) {
      return false;
    }

    delete(ban);
    Range range = Range.closed(ban.getFromIp(), ban.getToIp());

    bans.remove(range);
    ranges.remove(range);

    plugin.getIpRangeBanRecordStorage().addRecord(ban, actor, reason);

    return true;
  }

  public CloseableIterator<IpRangeBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpRangeBanData, Integer> query = queryBuilder();
    Where<IpRangeBanData, Integer> where = query.where();
    where
        .ge("created", checkTime)
        .or()
        .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }
}
