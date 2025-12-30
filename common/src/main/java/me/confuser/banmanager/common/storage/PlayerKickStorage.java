package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerKickData;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;

import java.sql.SQLException;

public class PlayerKickStorage extends BaseStorage<PlayerKickData, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public PlayerKickStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<PlayerKickData>) plugin.getConfig()
                                                                             .getLocalDb().getTable("playerKicks"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " CHANGE `created` `created` BIGINT UNSIGNED");
      } catch (SQLException e) {
      }
    }
  }

  public PlayerKickStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<PlayerKickData>) table, plugin.getConfig().getLocalDb());
  }

  public boolean addKick(PlayerKickData data, boolean isSilent) throws SQLException {
    if (create(data) != 1) return false;

    plugin.getServer().callEvent("PlayerKickedEvent", data, isSilent);

    return true;
  }

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    updateRaw("DELETE FROM " + getTableInfo().getTableName() + " WHERE created < UNIX_TIMESTAMP(CURRENT_TIMESTAMP - INTERVAL '" + cleanup.getDays() + "' DAY)");
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
