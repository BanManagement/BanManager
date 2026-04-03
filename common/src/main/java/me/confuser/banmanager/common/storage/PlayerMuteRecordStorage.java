package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerMuteRecord;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;

public class PlayerMuteRecordStorage extends BaseStorage<PlayerMuteRecord, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public PlayerMuteRecordStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<PlayerMuteRecord>) plugin.getConfig()
        .getLocalDb()
        .getTable("playerMuteRecords"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public PlayerMuteRecordStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<PlayerMuteRecord>) table, plugin.getConfig().getLocalDb());
  }

  public void addRecord(PlayerMuteData mute, PlayerData actor, String reason) throws SQLException {
    create(new PlayerMuteRecord(mute, actor, reason));
  }

  public CloseableIterator<PlayerMuteRecord> findUnmutes(long fromTime) throws SQLException {
    if (fromTime == 0) {
      return iterator();
    }

    QueryBuilder<PlayerMuteRecord, Integer> query = queryBuilder();
    Where<PlayerMuteRecord, Integer> where = query.where();

    where.ge("created", fromTime);

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

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(CURRENT_TIMESTAMP - INTERVAL '" + cleanup.getDays() + "' DAY)");
  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerMuteRecord, Integer> builder = deleteBuilder();

    builder.where().eq("player_id", player);

    return builder.delete();
  }
}
