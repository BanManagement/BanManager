package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.IpBanRecord;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.StorageUtils;

import java.sql.SQLException;

public class IpBanRecordStorage extends BaseDaoImpl<IpBanRecord, Integer> {

  public IpBanRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<IpBanRecord>) plugin.getConfig().getLocalDb()
        .getTable("ipBanRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `createdReason` VARCHAR(255)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `silent` TINYINT(1)");
      } catch (SQLException e) {
      }

      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `created` `created` BIGINT UNSIGNED,"
          + " CHANGE `pastCreated` `pastCreated` BIGINT UNSIGNED,"
          + " CHANGE `expired` `expired` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }

      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "ip");
    }
  }

  public IpBanRecordStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<IpBanRecord>) table);
  }

  public void addRecord(IpBanData ban, PlayerData actor, String reason) throws SQLException {
    create(new IpBanRecord(ban, actor, reason));
  }

  public CloseableIterator<IpBanRecord> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpBanRecord, Integer> query = queryBuilder();
    Where<IpBanRecord, Integer> where = query.where();

    where.ge("created", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

  public long getCount(IPAddress ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).countOf();
  }

  public CloseableIterator<IpBanRecord> getRecords(IPAddress ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).iterator();
  }

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(CURRENT_TIMESTAMP - INTERVAL '" + cleanup.getDays() + "' DAY)");
  }

  public int deleteAll(IPAddress ip) throws SQLException {
    DeleteBuilder<IpBanRecord, Integer> builder = deleteBuilder();

    builder.where().eq("ip", ip);

    return builder.delete();
  }
}
