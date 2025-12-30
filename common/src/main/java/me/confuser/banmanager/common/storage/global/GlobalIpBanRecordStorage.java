package me.confuser.banmanager.common.storage.global;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.global.GlobalIpBanRecordData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.storage.BaseStorage;

import java.sql.SQLException;

public class GlobalIpBanRecordStorage extends BaseStorage<GlobalIpBanRecordData, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public GlobalIpBanRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getGlobalConn(), (DatabaseTableConfig<GlobalIpBanRecordData>) plugin.getConfig()
                                                                                    .getGlobalDb()
                                                                                    .getTable("ipUnbans"), plugin.getConfig().getGlobalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " CHANGE `created` `created` BIGINT UNSIGNED");
      } catch (SQLException e) {
      }
    }
  }

  public CloseableIterator<GlobalIpBanRecordData> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<GlobalIpBanRecordData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", fromTime));

    return query.iterator();

  }
}
