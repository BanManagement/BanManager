package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.CleanUp;
import me.confuser.banmanager.data.PlayerData;
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

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(DATE_SUB(NOW(), " +
            "INTERVAL " + cleanup.getDays() + " DAY))");
  }

  public long getCount(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).countOf();
  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerKickData, Integer> builder = deleteBuilder();

    builder.where().eq("player_id", player);

    return builder.delete();
  }
}
