package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.RollbackData;
import me.confuser.banmanager.common.util.DateUtils;

import java.sql.SQLException;

public class RollbackStorage extends BaseDaoImpl<RollbackData, Integer> {

  private BanManagerPlugin plugin;

  public RollbackStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<RollbackData>) plugin.getConfig().getLocalDb()
                                                                           .getTable("rollbacks"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
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
