package me.confuser.banmanager.common.storage.global;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.storage.BaseStorage;

import java.sql.SQLException;

public class GlobalPlayerMuteStorage extends BaseStorage<GlobalPlayerMuteData, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public GlobalPlayerMuteStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getGlobalConn(), (DatabaseTableConfig<GlobalPlayerMuteData>) plugin.getConfig()
                                                                                   .getGlobalDb()
                                                                                   .getTable("playerMutes"), plugin.getConfig().getGlobalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig
                .getTableName() + " ADD COLUMN `soft` TINYINT(1)," +
                " ADD KEY `" + tableConfig.getTableName() + "_soft_idx` (`soft`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }

      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `created` `created` BIGINT UNSIGNED,"
          + " CHANGE `expires` `expires` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }
    }
  }

  public CloseableIterator<GlobalPlayerMuteData> findMutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<GlobalPlayerMuteData, Integer> query = queryBuilder();
    query.setWhere(query.where().ge("created", fromTime));

    return query.iterator();

  }
}
