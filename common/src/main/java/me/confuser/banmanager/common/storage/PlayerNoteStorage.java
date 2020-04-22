package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerNoteStorage extends BaseDaoImpl<PlayerNoteData, Integer> {

  private BanManagerPlugin plugin;

  public PlayerNoteStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerNoteData>) plugin.getConfig()
        .getLocalDb().getTable("playerNotes"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public PlayerNoteStorage(ConnectionSource connection, DatabaseTableConfig<?> playerNotes) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerNoteData>) playerNotes);
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
