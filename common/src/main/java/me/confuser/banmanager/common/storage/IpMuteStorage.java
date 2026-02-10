package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.AddressValueException;
import me.confuser.banmanager.common.ipaddr.IPAddress;
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
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.StorageUtils;
import me.confuser.banmanager.common.util.TransactionHelper;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IpMuteStorage extends BaseStorage<IpMuteData, Integer> {

  private ConcurrentHashMap<String, IpMuteData> mutes = new ConcurrentHashMap<>();

  public IpMuteStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<IpMuteData>) plugin.getConfig().getLocalDb()
        .getTable("ipMutes"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "ip");

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

    plugin.getLogger().info("Loaded " + mutes.size() + " ip mutes into memory");
  }

  public IpMuteStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<IpMuteData>) table, plugin.getConfig().getLocalDb());
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection("");
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve ip mutes into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, a.id, a.name, a.ip, a.lastSeen, t.ip, t.reason,");
    sql.append(" t.soft, t.expires, t.created, t.updated, t.silent");
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

      plugin.getLogger().warning("Failed to retrieve ip mutes into memory");
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
          plugin.getLogger().warning("Missing or invalid player for ip mute " + results.getInt(0) + ", ignored");
          continue;
        }

        IpMuteData mute = new IpMuteData(results.getInt(0), IPUtils.toIPAddress(results.getBytes(5)), actor, results.getString(6),
            results.getBoolean(11),
            results.getBoolean(7),
            results.getLong(8),
            results.getLong(9),
            results.getLong(10));

        mutes.put(mute.getIp().toString(), mute);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (results != null) results.closeQuietly();

      getConnectionSource().releaseConnection(connection);
    }
  }

  public ConcurrentHashMap<String, IpMuteData> getMutes() {
    return mutes;
  }

  public boolean isMuted(IPAddress ip) {
    return mutes.get(ip.toString()) != null;
  }

  public boolean isMuted(InetAddress address) {
    return isMuted(IPUtils.toIPAddress(address));
  }

  public IpMuteData retrieveMute(IPAddress ip) throws SQLException {
    List<IpMuteData> mutes = queryForEq("ip", ip);

    if (mutes.isEmpty()) return null;

    return mutes.get(0);
  }

  public IpMuteData getMute(IPAddress ip) {
    return mutes.get(ip.toString());
  }

  public IpMuteData getMute(InetAddress address) {
    return getMute(IPUtils.toIPAddress(address));
  }

  public void addMute(IpMuteData mute) {
    mutes.put(mute.getIp().toString(), mute);

    plugin.getServer().callEvent("IpMutedEvent", mute, mute.isSilent() || !plugin.getConfig().isBroadcastOnSync());
  }

  public void removeMute(IpMuteData mute) {
    removeMute(mute.getIp());
  }

  public void removeMute(IPAddress ip) {
    mutes.remove(ip);
  }

  public boolean mute(IpMuteData mute) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("IpMuteEvent", mute, mute.isSilent());

    if (event.isCancelled()) {
      return false;
    }

    create(mute);
    mutes.put(mute.getIp().toString(), mute);

    plugin.getServer().callEvent("IpMutedEvent", mute, event.isSilent());

    return true;
  }

  public boolean unmute(IpMuteData mute, PlayerData actor) throws SQLException {
    return unmute(mute, actor, "");
  }

  public boolean unmute(IpMuteData mute, PlayerData actor, String reason) throws SQLException {
    return unmute(mute, actor, reason, false);
  }

  public boolean unmute(IpMuteData mute, PlayerData actor, String reason, boolean silent) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("IpUnmutedEvent", mute, actor, reason, silent);

    if (event.isCancelled()) {
      return false;
    }

    TransactionHelper.runInTransaction(connectionSource, () -> {
      delete(mute);
      plugin.getIpMuteRecordStorage().addRecord(mute, actor, reason);
    });

    mutes.remove(mute.getIp().toString());

    return true;
  }

  public CloseableIterator<IpMuteData> findMutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<IpMuteData, Integer> query = queryBuilder();
    Where<IpMuteData, Integer> where = query.where();
    where
        .ge("created", fromTime)
        .or()
        .ge("updated", fromTime);

    query.setWhere(where);

    return query.iterator();

  }

}
