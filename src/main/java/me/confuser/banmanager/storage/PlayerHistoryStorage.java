package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerHistoryData;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHistoryStorage extends BaseDaoImpl<PlayerHistoryData, Integer> {

  private ConcurrentHashMap<UUID, PlayerHistoryData> players = new ConcurrentHashMap<>();

  public PlayerHistoryStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerHistoryData>) BanManager.getPlugin().getConfiguration()
                                                                         .getLocalDb().getTable("playerHistory"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public void create(PlayerData player) {
    players.put(player.getUUID(), new PlayerHistoryData(player));
  }

  public PlayerHistoryData remove(UUID uuid) {
    return players.remove(uuid);
  }

  public CloseableIterator<PlayerHistoryData> getSince(PlayerData player, long since, int page) throws SQLException {
    return queryBuilder().limit(10L).offset(10L * page)
                         .orderBy("join", false)
                         .where().ge("join", since).and().eq("player_id", player)
                         .iterator();
  }

  public void save() {
    for (PlayerHistoryData data : players.values()) {
      data.setLeave(System.currentTimeMillis() / 1000L);

      try {
        create(data);
      } catch (SQLException e) {
        e.printStackTrace();
        break; // Don't slow down shut down if problems with the connection
      }
    }
  }
}
