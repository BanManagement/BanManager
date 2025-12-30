package me.confuser.banmanager.common.storage.global;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanRecordData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.storage.BaseStorage;

import java.sql.SQLException;

public class GlobalPlayerBanRecordStorage extends BaseStorage<GlobalPlayerBanRecordData, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public GlobalPlayerBanRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getGlobalConn(), (DatabaseTableConfig<GlobalPlayerBanRecordData>) plugin.getConfig()
                                                                                        .getGlobalDb()
                                                                                        .getTable("playerUnbans"), plugin.getConfig().getGlobalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " CHANGE `created` `created` BIGINT UNSIGNED");
      } catch (SQLException e) {
      }
    }
  }

  public CloseableIterator<GlobalPlayerBanRecordData> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<GlobalPlayerBanRecordData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", fromTime));

    return query.iterator();

  }
}
