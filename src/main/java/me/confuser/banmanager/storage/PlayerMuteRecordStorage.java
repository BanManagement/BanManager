package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.CleanUp;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.data.PlayerMuteRecord;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class PlayerMuteRecordStorage extends BaseDaoImpl<PlayerMuteRecord, Integer> {

  public PlayerMuteRecordStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerMuteRecord>) BanManager.getPlugin().getConfiguration()
                                                                        .getLocalDb().getTable("playerMuteRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `createdReason` VARCHAR(255), "
        + " ADD COLUMN `soft` TINYINT(1)," +
          " ADD KEY `" + tableConfig.getTableName() + "_soft_idx` (`soft`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }
  }

  public void addRecord(PlayerMuteData mute, PlayerData actor, String reason) throws SQLException {
    create(new PlayerMuteRecord(mute, actor, reason));
  }

  public CloseableIterator<PlayerMuteRecord> findUnmutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<PlayerMuteRecord, Integer> query = queryBuilder();
    Where<PlayerMuteRecord, Integer> where = query.where();

    where.ge("created", checkTime);

    query.setWhere(where);

    return query.iterator();

  }

  public long getCount(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).countOf();
  }

  public CloseableIterator<PlayerMuteRecord> getRecords(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).iterator();
  }

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(DATE_SUB(NOW(), " +
            "INTERVAL " + cleanup.getDays() + " DAY))");
  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerMuteRecord, Integer> builder = deleteBuilder();

    builder.where().eq("player_id", player);

    return builder.delete();
  }
}
