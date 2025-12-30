package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerNoteStorage extends BaseStorage<PlayerNoteData, Integer> {

  @Override
  protected boolean hasUpdatedColumn() {
    return false;
  }

  public PlayerNoteStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<PlayerNoteData>) plugin.getConfig()
        .getLocalDb().getTable("playerNotes"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName() + " CHANGE `created` `created` BIGINT UNSIGNED");
      } catch (SQLException e) {
      }
    }
  }

  public PlayerNoteStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> playerNotes) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<PlayerNoteData>) playerNotes, plugin.getConfig().getLocalDb());
  }

  public boolean addNote(PlayerNoteData data) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("PlayerNoteCreatedEvent", data);

    return !event.isCancelled() && create(data) == 1;
  }

  public CloseableIterator<PlayerNoteData> getNotes(UUID uniqueId) throws SQLException {
    return queryBuilder().where().eq("player_id", UUIDUtils.toBytes(uniqueId)).iterator();
  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerNoteData, Integer> builder = deleteBuilder();

    builder.where().eq("player_id", player);

    return builder.delete();
  }

  public long getCount(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).countOf();
  }
}
