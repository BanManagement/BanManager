package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerReportCommentData;

import java.sql.SQLException;

public class PlayerReportCommentStorage extends BaseDaoImpl<PlayerReportCommentData, Integer> {

  public PlayerReportCommentStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerReportCommentData>) plugin.getConfig()
        .getLocalDb()
        .getTable("playerReportComments"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public PlayerReportCommentStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportCommentData>) table);
  }

  public PlayerReportCommentData getByReportId(int id) throws SQLException {
    return queryBuilder().where().eq("report_id", id).queryForFirst();
  }

}
