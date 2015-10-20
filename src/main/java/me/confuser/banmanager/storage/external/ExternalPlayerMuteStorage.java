package me.confuser.banmanager.storage.external;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.external.ExternalPlayerMuteData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class ExternalPlayerMuteStorage extends BaseDaoImpl<ExternalPlayerMuteData, Integer> {

  public ExternalPlayerMuteStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<ExternalPlayerMuteData>) BanManager.getPlugin().getConfiguration()
                                                                              .getExternalDb().getTable("playerMutes"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig
          .getTableName() + " ADD COLUMN `soft` TINYINT(1)," +
          " ADD KEY `" + tableConfig.getTableName() + "_soft_idx` (`soft`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }
  }

  public CloseableIterator<ExternalPlayerMuteData> findMutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<ExternalPlayerMuteData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", checkTime));

    return query.iterator();

  }
}
