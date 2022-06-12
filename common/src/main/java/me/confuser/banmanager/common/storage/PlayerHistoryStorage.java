package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.CleanUp;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import me.confuser.banmanager.common.ormlite.dao.BaseDaoImpl;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.StorageUtils;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerHistoryStorage extends BaseDaoImpl<PlayerHistoryData, Integer> {

  private ConcurrentHashMap<UUID, PlayerHistoryData> players = new ConcurrentHashMap<>();

  public PlayerHistoryStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerHistoryData>) plugin.getConfig()
        .getLocalDb()
        .getTable("playerHistory"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    } else {
      try {
        executeRawNoArgs("ALTER TABLE " + tableConfig.getTableName()
          + " CHANGE `join` `join` BIGINT UNSIGNED,"
          + " CHANGE `leave` `leave` BIGINT UNSIGNED"
        );
      } catch (SQLException e) {
      }

      StorageUtils.convertIpColumn(plugin, tableConfig.getTableName(), "ip");
    }
  }

  public PlayerHistoryStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerHistoryData>) table);
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

  public void purge(CleanUp cleanup) throws SQLException {
    if (cleanup.getDays() == 0) return;

    String banTable = BanManagerPlugin.getInstance().getIpBanStorage()
        .getTableInfo()
        .getTableName();

    // H2 does not support DELETE FROM joins sadly
    CloseableIterator<String[]> results = queryRaw("SELECT ph.id FROM " + getTableInfo()
        .getTableName() + " AS ph LEFT JOIN " + banTable + " b ON ph.ip = b.ip WHERE b.ip IS NULL AND ph.leave < " +
        "UNIX_TIMESTAMP(CURRENT_TIMESTAMP - INTERVAL '" + cleanup.getDays() + "' DAY)").closeableIterator();

    while (results.hasNext()) {
      int id = Integer.parseInt(results.next()[0]);

      deleteById(id);
    }

    results.closeQuietly();
  }
}
