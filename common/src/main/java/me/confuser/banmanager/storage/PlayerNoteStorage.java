package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.events.PlayerNoteCreatedEvent;
import me.confuser.banmanager.util.UUIDUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerNoteStorage extends BaseDaoImpl<PlayerNoteData, Integer> {

  private BanManagerPlugin plugin = BanManager.getPlugin();

  public PlayerNoteStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerNoteData>) BanManager.getPlugin().getConfiguration()
                                                                      .getLocalDb().getTable("playerNotes"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public boolean addNote(PlayerNoteData data) throws SQLException {
    PlayerNoteCreatedEvent event = new PlayerNoteCreatedEvent(data);
    Bukkit.getServer().getPluginManager().callEvent(event);

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
}
