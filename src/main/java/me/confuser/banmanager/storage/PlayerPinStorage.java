package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerPinData;
import me.confuser.banmanager.data.PlayerReportLocationData;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class PlayerPinStorage extends BaseDaoImpl<PlayerPinData, Integer> {

  public PlayerPinStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerPinData>) BanManager.getPlugin().getConfiguration()
                                                                                .getLocalDb()
                                                                                .getTable("playerPins"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public PlayerPinData generate(PlayerData player) {
    PlayerPinData pin = null;
    try {
      pin = new PlayerPinData(player);
      if (create(pin) != 1) {
        pin = null;
      }
    } catch (NoSuchAlgorithmException | SQLException e) {
      e.printStackTrace();
    }

    return pin;
  }

}
