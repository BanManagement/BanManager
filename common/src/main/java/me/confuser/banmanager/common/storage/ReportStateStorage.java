package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.ReportState;

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

}
