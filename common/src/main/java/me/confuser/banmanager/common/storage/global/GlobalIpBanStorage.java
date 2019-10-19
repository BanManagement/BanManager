package me.confuser.banmanager.common.storage.global;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.global.GlobalIpBanData;
import me.confuser.banmanager.common.util.DateUtils;

import java.sql.SQLException;

public class GlobalIpBanStorage extends BaseDaoImpl<GlobalIpBanData, Integer> {

  public GlobalIpBanStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getGlobalConn(), (DatabaseTableConfig<GlobalIpBanData>) plugin.getConfig().getGlobalDb()
                                                                              .getTable("ipBans"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public CloseableIterator<GlobalIpBanData> findBans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<GlobalIpBanData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", checkTime));

    return query.iterator();

  }
}
