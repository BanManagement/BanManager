package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.StatementBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.StorageUtils;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class IpMuteStorage extends BaseDaoImpl<IpMuteData, Integer> {

  private BanManagerPlugin plugin;
  private ConcurrentHashMap<String, IpMuteData> mutes = new ConcurrentHashMap<>();

  public IpMuteStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<IpMuteData>) plugin.getConfig().getLocalDb()
        .getTable("ipMutes"));

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

    plugin.getLogger().info("Loaded " + mutes.size() + " ip mutes into memory");
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
        PlayerData actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(1)), results.getString(2),
            IPUtils.toIPAddress(results.getBytes(3)),
            results.getLong(4));
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
    CommonEvent event = plugin.getServer().callEvent("IpUnmutedEvent", mute, actor, reason);

    if (event.isCancelled()) {
      return false;
    }

    delete(mute);
    mutes.remove(mute.getIp().toString());

    plugin.getIpMuteRecordStorage().addRecord(mute, actor, reason);

    return true;
  }

  public CloseableIterator<IpMuteData> findMutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpMuteData, Integer> query = queryBuilder();
    Where<IpMuteData, Integer> where = query.where();
    where
        .ge("created", checkTime)
        .or()
        .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

}
