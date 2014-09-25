package me.confuser.banmanager.storage;

import java.sql.SQLException;

import me.confuser.banmanager.data.PlayerKickData;
import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;

public class PlayerKickStorage extends BaseDaoImpl<PlayerKickData, Integer> {

      public PlayerKickStorage(ConnectionSource connection, DatabaseTableConfig<PlayerKickData> tableConfig) throws SQLException {
            super(connection, tableConfig);
      }

      public boolean addKick(PlayerKickData data) throws SQLException {
            return create(data) == 1;
      }
}
