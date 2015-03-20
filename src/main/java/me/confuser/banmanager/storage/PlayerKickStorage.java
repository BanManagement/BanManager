package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerKickData;

import java.sql.SQLException;

public class PlayerKickStorage extends BaseDaoImpl<PlayerKickData, Integer> {

  public PlayerKickStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerKickData>) BanManager.getPlugin().getConfiguration()
                                                                      .getLocalDb().getTable("playerKicks"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public boolean addKick(PlayerKickData data) throws SQLException {
    return create(data) == 1;
  }
}
