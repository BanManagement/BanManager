package me.confuser.banmanager.common.storage;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.util.ReportList;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PlayerReportStorage extends BaseDaoImpl<PlayerReportData, Integer> {

  private BanManagerPlugin plugin;

  public PlayerReportStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin.getLocalConn(), (DatabaseTableConfig<PlayerReportData>) plugin.getConfig()
        .getLocalDb().getTable("playerReports"));

    this.plugin = plugin;

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
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
      try {
        String update = "ALTER TABLE " + tableConfig.getTableName() + " MODIFY assignee_id BINARY(16) NULL";
        executeRawNoArgs(update);
      } catch (SQLException e) {
      }
    }
  }

  public PlayerReportStorage(ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(connection, (DatabaseTableConfig<PlayerReportData>) table);
  }

  public boolean report(PlayerReportData data, boolean isSilent) throws SQLException {
    CommonEvent event = plugin.getServer().callEvent("PlayerReportEvent", data, isSilent);

    if (event.isCancelled()) {
      return false;
    }

    if (create(data) != 1) return false;

    plugin.getServer().callEvent("PlayerReportedEvent", data, isSilent);

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

    long pageSize = 5L;
    long count = query.countOf();
    long maxPage = count == 0 ? 1 : (int) Math.ceil((double) count / pageSize);

    if (maxPage == 0) maxPage = 1;

    long offset = (page - 1) * pageSize;

    query.reset();

    query.offset(offset).limit(pageSize);

    return new ReportList(query.query(), count, maxPage);
  }

  public ReportList getReports(long page, int state) throws SQLException {
    return getReports(page, state, null);
  }

  public int deleteAll(PlayerData player) throws SQLException {
    List<PlayerReportData> reports = queryForEq("player_id", player);

    for (PlayerReportData report : reports) {
      deleteById(report.getId());
    }

    return reports.size();
  }

  public boolean isRecentlyReported(PlayerData player) throws SQLException {
    if (plugin.getConfig().getReportCooldown() == 0) {
      return false;
    }

    return queryBuilder().where()
        .eq("player_id", player).and()
        .ge("created", (System.currentTimeMillis() / 1000L) - plugin.getConfig()
            .getReportCooldown())
        .countOf() > 0;
  }

  public int deleteById(Integer id) throws SQLException {
    PlayerReportData report = queryForId(id);

    if (report == null) return 0;

    plugin.getServer().callEvent("PlayerReportDeletedEvent", report);

    super.deleteById(id);

    return 1;
  }

  public int deleteIds(Collection<Integer> ids) throws SQLException {
    if (ids == null || ids.isEmpty()) return 0;

    int count = 0;

    for (Integer id : ids) {
      if (deleteById(id) != 0) count++;
    }

    return count;
  }
}
