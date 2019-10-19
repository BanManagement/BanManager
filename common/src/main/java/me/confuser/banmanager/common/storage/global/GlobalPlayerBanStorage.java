package me.confuser.banmanager.common.storage.global;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanData;
import me.confuser.banmanager.common.util.DateUtils;

import java.sql.SQLException;

public class GlobalPlayerBanStorage extends BaseDaoImpl<GlobalPlayerBanData, Integer> {

  public GlobalPlayerBanStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getGlobalConn(), (DatabaseTableConfig<GlobalPlayerBanData>) plugin.getConfig()
                                                                                  .getGlobalDb()
                                                                                  .getTable("playerBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
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
