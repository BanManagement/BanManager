package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerReportCommentData;

import java.sql.SQLException;

public class PlayerReportCommentStorage extends BaseDaoImpl<PlayerReportCommentData, Integer> {

  public PlayerReportCommentStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportCommentData>) BanManager.getPlugin()
                                                                                .getLocalDb()
                                                                                .getTable("playerReportComments"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public PlayerReportCommentData getByReportId(int id) throws SQLException {
    return queryBuilder().where().eq("report_id", id).queryForFirst();
  }

}
