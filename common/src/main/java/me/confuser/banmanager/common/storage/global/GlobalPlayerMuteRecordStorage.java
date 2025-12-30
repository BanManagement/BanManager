package me.confuser.banmanager.common.storage.global;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteRecordData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.storage.BaseStorage;

import java.sql.SQLException;

public class GlobalPlayerMuteRecordStorage extends BaseStorage<GlobalPlayerMuteRecordData, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public GlobalPlayerMuteRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getGlobalConn(), (DatabaseTableConfig<GlobalPlayerMuteRecordData>) plugin.getConfig()
                                                                                         .getGlobalDb()
                                                                                         .getTable("playerUnmutes"), plugin.getConfig().getGlobalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " CHANGE `created` `created` BIGINT UNSIGNED");
      } catch (SQLException e) {
      }
    }
  }

  public CloseableIterator<GlobalPlayerMuteRecordData> findUnmutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<GlobalPlayerMuteRecordData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", fromTime));

    return query.iterator();

  }
}
