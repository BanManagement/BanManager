package me.confuser.banmanager.storage.global;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class GlobalPlayerBanStorage extends BaseDaoImpl<GlobalPlayerBanData, Integer> {

  public GlobalPlayerBanStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<GlobalPlayerBanData>) BanManager.getPlugin()
                                                                           .getGlobalDb().getTable("playerBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public CloseableIterator<GlobalPlayerBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<GlobalPlayerBanData, Integer> query = queryBuilder();
    query.setWhere(queryBuilder().where().ge("created", checkTime));

    return query.iterator();

  }
}
