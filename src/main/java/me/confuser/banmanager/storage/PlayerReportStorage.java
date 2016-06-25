package me.confuser.banmanager.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.CloseableIterator;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.commands.report.ReportList;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.data.ReportState;
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
    } else {
      try {
        String update = "ALTER TABLE " + tableConfig
                .getTableName() + " ADD COLUMN `state_id` INT(11) NOT NULL DEFAULT 1," +
                " ADD COLUMN `assignee_id` BINARY(16)," +
                " ADD KEY `" + tableConfig.getTableName() + "_state_id_idx` (`state_id`)," +
                " ADD KEY `" + tableConfig.getTableName() + "_assignee_id_idx` (`assignee_id`)";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
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

  public ReportList getReports(long page, Integer state, UUID uniqueId) throws SQLException {
    QueryBuilder<PlayerReportData, Integer> query = queryBuilder();

    if (state != null || uniqueId != null) {
      Where<PlayerReportData, Integer> where = query.where();

      if (state != null) where.eq("state_id", state);
      if (state != null && uniqueId != null) where.and();
      if (uniqueId != null) where.eq("actor_id", UUIDUtils.toBytes(uniqueId));
    }

    query.setCountOf(true);
    PreparedQuery<PlayerReportData> preparedQuery = query.prepare();

    long pageSize = 5L;
    long count = countOf(preparedQuery);
    long maxPage = count == 0 ? 1 : (int) Math.ceil(count / pageSize);

    if (maxPage == 0) maxPage = 1;

    long offset = (page - 1) * pageSize;

    query.setCountOf(false).offset(offset).limit(pageSize);

    return new ReportList(query.query(), count, maxPage);
  }

  public ReportList getReports(long page, int state) throws SQLException {
    return getReports(page, state, null);
  }

  public int deleteAll(PlayerData player) throws SQLException {
    DeleteBuilder<PlayerReportData, Integer> builder = deleteBuilder();

    Where<PlayerReportData, Integer> where = builder.where();
    where.eq("player_id", player);

    builder.setWhere(where);

    return builder.delete();
  }

  public boolean isRecentlyReported(PlayerData player) throws SQLException {
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
