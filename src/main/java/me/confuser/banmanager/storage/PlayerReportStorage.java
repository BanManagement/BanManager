package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.events.PlayerReportEvent;
import me.confuser.banmanager.events.PlayerReportedEvent;
import me.confuser.banmanager.util.UUIDUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class PlayerReportStorage extends BaseDaoImpl<PlayerReportData, Integer> {

  private BanManager plugin = BanManager.getPlugin();

  public PlayerReportStorage(ConnectionSource connection) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportData>) BanManager.getPlugin().getConfiguration()
                                                                        .getLocalDb().getTable("playerReports"));

    if (!this.isTableExists()) {
      TableUtils.createTable(connection, tableConfig);
    }
  }

  public boolean report(PlayerReportData data, boolean isSilent) throws SQLException {
    PlayerReportEvent event = new PlayerReportEvent(data, isSilent);

    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event.isCancelled()) {
      return false;
    }

    if (create(data) != 1) return false;

    Bukkit.getServer().getPluginManager().callEvent(new PlayerReportedEvent(data, isSilent));

    return true;
  }

  public CloseableIterator<PlayerReportData> getReports(UUID uniqueId) throws SQLException {
    return queryBuilder().where().eq("player_id", UUIDUtils.toBytes(uniqueId)).iterator();
  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerReportData, Integer> builder = deleteBuilder();

    Where<PlayerReportData, Integer> where = builder.where();
    where.eq("player_id", player);

    builder.setWhere(where);

    return builder.delete();
  }

  public boolean isRecentlyWarned(PlayerData player) throws SQLException {
    if (plugin.getConfiguration().getReportCooldown() == 0) {
      return false;
    }

    return queryBuilder().where()
                         .eq("player_id", player).and()
                         .ge("created", (System.currentTimeMillis() / 1000L) - plugin.getConfiguration()
                                                                                     .getReportCooldown())
                         .countOf() > 0;
  }
}
