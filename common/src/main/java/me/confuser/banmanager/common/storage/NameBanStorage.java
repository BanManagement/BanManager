package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.AddressValueException;
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
import me.confuser.banmanager.common.util.TransactionHelper;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class NameBanStorage extends BaseDaoImpl<NameBanData, Integer> {

  private BanManagerPlugin plugin;
  private ConcurrentHashMap<String, NameBanData> bans = new ConcurrentHashMap<>();

  public NameBanStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<NameBanData>) plugin.getConfig().getLocalDb()
        .getTable("nameBans"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
      return;
    } else {
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

    plugin.getLogger().info("Loaded " + bans.size() + " name bans into memory");
  }

  public NameBanStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<NameBanData>) table);
  }

  private void loadAll() throws SQLException {
    DatabaseConnection connection;

    try {
      connection = this.getConnectionSource().getReadOnlyConnection(getTableName());
    } catch (SQLException e) {
      e.printStackTrace();
      plugin.getLogger().warning("Failed to retrieve name bans into memory");
      return;
    }
    StringBuilder sql = new StringBuilder();

    sql.append("SELECT t.id, t.name, a.id, a.name, a.ip, a.lastSeen, t.reason,");
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

      plugin.getLogger().warning("Failed to retrieve name bans into memory");
      return;
    }

    DatabaseResults results = null;

    try {
      results = statement.runQuery(null);

      while (results.next()) {
        PlayerData actor;

        try {
          actor = new PlayerData(UUIDUtils.fromBytes(results.getBytes(2)), results.getString(3),
              IPUtils.toIPAddress(results.getBytes(4)),
              results.getLong(5));
        } catch (NullPointerException | AddressValueException e) {
          plugin.getLogger().warning("Missing or invalid actor for ban " + results.getInt(0) + ", ignored");
          continue;
        }

        NameBanData ban = new NameBanData(results.getInt(0),
            results.getString(1),
            actor,
            results.getString(6),
            results.getBoolean(10),
            results.getLong(7),
            results.getLong(8),
            results.getLong(9));

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

  public boolean ban(NameBanData ban) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("NameBanEvent", ban, ban.isSilent());

    if (event.isCancelled()) {
      return false;
    }

    create(ban);
    bans.put(ban.getName().toLowerCase(), ban);

    plugin.getServer().callEvent("NameBannedEvent", ban, event.isSilent());

    return true;
  }

  public boolean unban(NameBanData ban, PlayerData actor) throws SQLException {
    return unban(ban, actor, "");
  }

  public boolean unban(NameBanData ban, PlayerData actor, String reason) throws SQLException {
    return unban(ban, actor, reason, false);
  }

  public boolean unban(NameBanData ban, PlayerData actor, String reason, boolean delete) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("NameUnbanEvent", ban, actor, reason);

    if (event.isCancelled()) {
      return false;
    }

    TransactionHelper.runInTransaction(connectionSource, () -> {
      delete(ban);
      if (!delete) plugin.getNameBanRecordStorage().addRecord(ban, actor, reason);
    });

    bans.remove(ban.getName().toLowerCase());

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
