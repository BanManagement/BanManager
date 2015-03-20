package me.confuser.banmanager.storage.external;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.external.ExternalPlayerBanData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExternalPlayerBanStorage extends BaseDaoImpl<ExternalPlayerBanData, Integer> {

  public ExternalPlayerBanStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<ExternalPlayerBanData>) BanManager.getPlugin().getConfiguration()
                                                                             .getExternalDb().getTable("playerBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public CloseableIterator<ExternalPlayerBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<ExternalPlayerBanData, Integer> query = queryBuilder();
    query.setWhere(queryBuilder().where().ge("created", checkTime));

    return query.iterator();

  }
}
