package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerReportLocationData;

import java.sql.SQLException;

public class PlayerReportLocationStorage extends BaseDaoImpl<PlayerReportLocationData, Integer> {

  public PlayerReportLocationStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportLocationData>) BanManager.getPlugin().getConfiguration()
                                                                                .getLocalDb()
                                                                                .getTable("playerReportLocations"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

}
