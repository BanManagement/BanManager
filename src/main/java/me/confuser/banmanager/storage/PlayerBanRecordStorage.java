package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.CleanUp;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerBanRecord;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;

public class PlayerBanRecordStorage extends BaseDaoImpl<PlayerBanRecord, Integer> {

  public PlayerBanRecordStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerBanRecord>) BanManager.getPlugin().getConfiguration()
                                                                       .getLocalDb().getTable("playerBanRecords"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
      return;
    } else {
      // Attempt to add new columns
      try {
        String update = "ALTER TABLE " + tableConfig.getTableName() + " ADD COLUMN `createdReason` VARCHAR(255)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }
  }

  public void addRecord(PlayerBanData ban, PlayerData actor, String reason) throws SQLException {
    create(new PlayerBanRecord(ban, actor, reason));
  }

  public CloseableIterator<PlayerBanRecord> findUnbans(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    long checkTime = fromTime + DateUtils.getTimeDiff();

    QueryBuilder<PlayerBanRecord, Integer> query = queryBuilder();
    Where<PlayerBanRecord, Integer> where = query.where();

    where.ge("created", checkTime);

    query.setWhere(where);



    return query.iterator();

  }

  public long getCount(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).countOf();
  }

  public CloseableIterator<PlayerBanRecord> getRecords(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).iterator();
  }

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(DATE_SUB(NOW(), " +
            "INTERVAL " + cleanup.getDays() + " DAY))");
  }
}
