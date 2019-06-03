package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.ReportState;

import java.sql.SQLException;

public class ReportStateStorage extends BaseDaoImpl<ReportState, Integer> {

  public ReportStateStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<ReportState>) BanManager.getPlugin()
                                                                                .getLocalDb()
                                                                                .getTable("playerReportStates"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);

      create(new ReportState("Open"));
      create(new ReportState("Assigned"));
      create(new ReportState("Resolved"));
      create(new ReportState("Closed"));
    }
  }

}
