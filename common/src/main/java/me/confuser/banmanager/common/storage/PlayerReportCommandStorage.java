package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerReportCommandData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;

public class PlayerReportCommandStorage extends BaseDaoImpl<PlayerReportCommandData, Integer> {

  public PlayerReportCommandStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerReportCommandData>) plugin.getConfig()
                                                                                      .getLocalDb()
                                                                                      .getTable("playerReportCommands"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `created` `created` BIGINT UNSIGNED,"
          + " CHANGE `updated` `updated` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }
    }
  }

  public PlayerReportCommandStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportCommandData>) table);
  }

  public PlayerReportCommandData getByReportId(int id) throws SQLException {
    return queryBuilder().where().eq("report_id", id).queryForFirst();
  }

}
