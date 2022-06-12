package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.IpRangeBanRecord;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.field.SqlType;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.SelectArg;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.StorageUtils;

import java.sql.SQLException;

public class IpRangeBanRecordStorage extends BaseDaoImpl<IpRangeBanRecord, Integer> {

  public IpRangeBanRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<IpRangeBanRecord>) plugin.getConfig().getLocalDb()
        .getTable("ipRangeBanRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `createdReason` VARCHAR(255)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }

      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "fromIp");
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "toIp");

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
    }
  }

  public IpRangeBanRecordStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<IpRangeBanRecord>) table);
  }

  public void addRecord(IpRangeBanData ban, PlayerData actor, String reason) throws SQLException {
    create(new IpRangeBanRecord(ban, actor, reason));
  }

  public CloseableIterator<IpRangeBanRecord> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpRangeBanRecord, Integer> query = queryBuilder();
    Where<IpRangeBanRecord, Integer> where = query.where();

    where.ge("created", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

  public long getCount(IPAddress ip) throws SQLException {
    return queryBuilder().where().raw("? BETWEEN fromIp AND toIp", new SelectArg(SqlType.BYTE_ARRAY, ip.getBytes())).countOf();
  }

  public CloseableIterator<IpRangeBanRecord> getRecords(IPAddress ip) throws SQLException {
    return queryBuilder().where().raw("? BETWEEN fromIp AND toIp", new SelectArg(SqlType.BYTE_ARRAY, ip.getBytes())).iterator();
  }
}
