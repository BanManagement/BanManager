package me.confuser.banmanager.storage.external;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.data.external.ExternalPlayerMuteRecordData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExternalPlayerMuteRecordStorage extends BaseDaoImpl<ExternalPlayerMuteRecordData, Integer> {

  public ExternalPlayerMuteRecordStorage(ConnectionSource connection, DatabaseTableConfig<ExternalPlayerMuteRecordData>
          tableConfig) throws SQLException {
    super(connection, tableConfig);
  }

  public CloseableIterator<ExternalPlayerMuteRecordData> findUnmutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<ExternalPlayerMuteRecordData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", checkTime));

    return query.iterator();

  }
}
