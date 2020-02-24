package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import inet.ipaddr.IPAddress;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.IpMuteRecord;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.StorageUtils;

import java.sql.SQLException;

public class IpMuteRecordStorage extends BaseDaoImpl<IpMuteRecord, Integer> {

  public IpMuteRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<IpMuteRecord>) plugin.getConfig().getLocalDb()
        .getTable("ipMuteRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "ip");

      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `silent` TINYINT(1)");
      } catch (SQLException e) {
      }
    }
  }

  public IpMuteRecordStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<IpMuteRecord>) table);
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

  public long getCount(IPAddress ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).countOf();
  }

  public CloseableIterator<IpMuteRecord> getRecords(IPAddress ip) throws SQLException {
    return queryBuilder().where().eq("ip", ip).iterator();
  }

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(DATE_SUB(NOW(), " +
        "INTERVAL " + cleanup.getDays() + " DAY))");
  }
}
