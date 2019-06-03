package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerReportCommandData;

import java.sql.SQLException;

public class PlayerReportCommandStorage extends BaseDaoImpl<PlayerReportCommandData, Integer> {

  public PlayerReportCommandStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportCommandData>) BanManager.getPlugin()
                                                                                .getLocalDb()
                                                                                .getTable("playerReportCommands"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public PlayerReportCommandData getByReportId(int id) throws SQLException {
    return queryBuilder().where().eq("report_id", id).queryForFirst();
  }

}
