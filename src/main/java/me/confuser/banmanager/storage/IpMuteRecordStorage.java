package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.CleanUp;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.IpMuteRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class IpMuteRecordStorage extends BaseDaoImpl<IpMuteRecord, Integer> {

  public IpMuteRecordStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<IpMuteRecord>) BanManager.getPlugin().getConfiguration().getLocalDb()
                                                                   .getTable("ipMuteRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public void addRecord(IpMuteData mute, PlayerData actor, String reason) throws SQLException {
    create(new IpMuteRecord(mute, actor, reason));
  }

  public CloseableIterator<IpMuteRecord> findUnmutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<IpMuteRecord, Integer> query = queryBuilder();
    Where<IpMuteRecord, Integer> where = query.where();

    where.ge("created", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

  public long getCount(long ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).countOf();
  }

  public CloseableIterator<IpMuteRecord> getRecords(long ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).iterator();
  }

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(DATE_SUB(NOW(), " +
            "INTERVAL " + cleanup.getDays() + " DAY))");
  }
}
