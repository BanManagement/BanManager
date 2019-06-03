package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.data.RollbackData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class RollbackStorage extends BaseDaoImpl<RollbackData, Integer> {

  private BanManagerPlugin plugin = BanManager.getPlugin();

  public RollbackStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<RollbackData>) BanManager.getPlugin().getLocalDb()
                                                                    .getTable("rollbacks"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public CloseableIterator<RollbackData> findRollbacks(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<RollbackData, Integer> query = queryBuilder();
    Where<RollbackData, Integer> where = query.where();
    where.ge("created", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

}
