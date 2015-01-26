package me.confuser.banmanager.storage.external;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.data.external.ExternalIpBanData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExternalIpBanStorage extends BaseDaoImpl<ExternalIpBanData, Integer> {

  public ExternalIpBanStorage(ConnectionSource connection, DatabaseTableConfig<ExternalIpBanData>
          tableConfig) throws SQLException {
    super(connection, tableConfig);
  }

  public CloseableIterator<ExternalIpBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<ExternalIpBanData, Integer> query = queryBuilder();
    Where<ExternalIpBanData, Integer> where = query.where();
    where
            .ge("created", checkTime)
            .or()
            .ge("updated", checkTime);

    query.setWhere(where);

    return query.iterator();

  }
}
