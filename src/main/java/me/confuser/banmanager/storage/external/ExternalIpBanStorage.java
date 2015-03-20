package me.confuser.banmanager.storage.external;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.external.ExternalIpBanData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExternalIpBanStorage extends BaseDaoImpl<ExternalIpBanData, Integer> {

  public ExternalIpBanStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<ExternalIpBanData>) BanManager.getPlugin().getConfiguration().getExternalDb()
                                                                         .getTable("ipBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public CloseableIterator<ExternalIpBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<ExternalIpBanData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", checkTime));

    return query.iterator();

  }
}
