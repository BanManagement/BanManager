package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.IpRangeBanRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class IpRangeBanRecordStorage extends BaseDaoImpl<IpRangeBanRecord, Integer> {

  public IpRangeBanRecordStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<IpRangeBanRecord>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                                        .getTable("ipRangeBanRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `createdReason` VARCHAR(255)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }
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

  public long getCount(long ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).countOf();
  }

  public CloseableIterator<IpRangeBanRecord> getRecords(long ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).iterator();
  }
}
