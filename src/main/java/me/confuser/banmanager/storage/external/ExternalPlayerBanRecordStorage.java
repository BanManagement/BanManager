package me.confuser.banmanager.storage.external;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.external.ExternalPlayerBanRecordData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExternalPlayerBanRecordStorage extends BaseDaoImpl<ExternalPlayerBanRecordData, Integer> {

  public ExternalPlayerBanRecordStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<ExternalPlayerBanRecordData>) BanManager.getPlugin().getConfiguration()
                                                                                   .getExternalDb()
                                                                                   .getTable("playerUnbans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public CloseableIterator<ExternalPlayerBanRecordData> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<ExternalPlayerBanRecordData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", checkTime));

    return query.iterator();

  }
}
