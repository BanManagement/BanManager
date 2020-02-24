package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerReportCommandData;

import java.sql.SQLException;

public class PlayerReportCommandStorage extends BaseDaoImpl<PlayerReportCommandData, Integer> {

  public PlayerReportCommandStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerReportCommandData>) plugin.getConfig()
                                                                                      .getLocalDb()
                                                                                      .getTable("playerReportCommands"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public PlayerReportCommandStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportCommandData>) table);
  }

  public PlayerReportCommandData getByReportId(int id) throws SQLException {
    return queryBuilder().where().eq("report_id", id).queryForFirst();
  }

}
