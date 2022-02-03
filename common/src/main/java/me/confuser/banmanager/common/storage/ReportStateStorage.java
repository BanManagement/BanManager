package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.ReportState;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;

public class ReportStateStorage extends BaseDaoImpl<ReportState, Integer> {

  public ReportStateStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<ReportState>) plugin.getConfig()
        .getLocalDb()
        .getTable("playerReportStates"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);

      create(new ReportState("Open"));
      create(new ReportState("Assigned"));
      create(new ReportState("Resolved"));
      create(new ReportState("Closed"));
    }
  }

  public ReportStateStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<ReportState>) table);
  }
}
