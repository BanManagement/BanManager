package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerReportLocationData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.List;

public class PlayerReportLocationStorage extends BaseDaoImpl<PlayerReportLocationData, Integer> {

  public PlayerReportLocationStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerReportLocationData>) plugin.getConfig()
        .getLocalDb()
        .getTable("playerReportLocations"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public PlayerReportLocationStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportLocationData>) table);
  }

  public PlayerReportLocationData getByReportId(int id) throws SQLException {
    return queryBuilder().where().eq("report_id", id).queryForFirst();
  }

  public List<PlayerReportLocationData> getByReport(PlayerReportData data) throws SQLException {
    return queryBuilder().where().eq("report_id", data.getId()).query();
  }

}
