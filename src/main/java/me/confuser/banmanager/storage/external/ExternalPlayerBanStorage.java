package me.confuser.banmanager.storage.external;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.data.external.ExternalPlayerBanData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExternalPlayerBanStorage extends BaseDaoImpl<ExternalPlayerBanData, Integer> {

  public ExternalPlayerBanStorage(ConnectionSource connection, DatabaseTableConfig<ExternalPlayerBanData>
          tableConfig) throws SQLException {
    super(connection, tableConfig);
  }

  public CloseableIterator<ExternalPlayerBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<ExternalPlayerBanData, Integer> query = queryBuilder();
    Where<ExternalPlayerBanData, Integer> where = query.where();
    where
            .ge("created", checkTime)
            .or()
            .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }
}
