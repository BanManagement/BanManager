package me.confuser.banmanager.common.storage;

import me.confuser.banmanager.api.event.player.PlayerReportDeletedEvent;
import me.confuser.banmanager.api.event.player.PlayerReportEvent;
import me.confuser.banmanager.api.event.player.PlayerReportedEvent;
import me.confuser.banmanager.api.request.ReportRequest;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.ormlite.stmt.DeleteBuilder;
import me.confuser.banmanager.common.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.ormlite.stmt.Where;
import me.confuser.banmanager.common.ormlite.support.ConnectionSource;
import me.confuser.banmanager.common.ormlite.table.DatabaseTableConfig;
import me.confuser.banmanager.common.ormlite.table.TableUtils;
import me.confuser.banmanager.common.util.ReportList;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PlayerReportStorage extends BaseStorage<PlayerReportData, Integer> {

  public PlayerReportStorage(BanManagerPlugin plugin) throws SQLException {
    super(plugin, plugin.getLocalConn(), (DatabaseTableConfig<PlayerReportData>) plugin.getConfig()
        .getLocalDb().getTable("playerReports"), plugin.getConfig().getLocalDb());

    if (!this.isTableExists()) {
      TableUtils.createTable(connectionSource, tableConfig);
    }
  }

  public PlayerReportStorage(BanManagerPlugin plugin, ConnectionSource connection, DatabaseTableConfig<?> table) throws SQLException {
    super(plugin, connection, (DatabaseTableConfig<PlayerReportData>) table, plugin.getConfig().getLocalDb());
  }

  public boolean report(PlayerReportData data, boolean isSilent) throws SQLException {
    ReportRequest request = EntityMappers.reportRequest(data);
    PlayerReportEvent pre = new PlayerReportEvent(request);
    plugin.getEventBus().publish(pre);

    if (pre.isCancelled()) {
      return false;
    }

    EntityMappers.applyTo(request, data);

    if (create(data) != 1) return false;

    plugin.getEventBus().publish(new PlayerReportedEvent(EntityMappers.playerReport(data)));

    return true;
  }

  public ReportList getReports(long page, Integer state, UUID uniqueId) throws SQLException {
    QueryBuilder<PlayerReportData, Integer> query = queryBuilder();
    Where<PlayerReportData, Integer> where = null;

    if (state != null || uniqueId != null) {
      where = query.where();

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
    query.orderBy("created", false).offset(offset).limit(pageSize);

    if (where != null) query.setWhere(where);

    return new ReportList(query.query(), count, maxPage);
  }

  public ReportList getReports(long page, int state) throws SQLException {
    return getReports(page, state, null);
  }

  public int deleteAll(PlayerData player, PlayerData actor) throws SQLException {
    List<PlayerReportData> reports = queryForEq("player_id", player);
    if (reports.isEmpty()) return 0;

    DeleteBuilder<PlayerReportData, Integer> builder = deleteBuilder();
    builder.where().eq("player_id", player);
    int deleted = builder.delete();

    if (deleted > 0 && actor != null) {
      for (PlayerReportData report : reports) {
        plugin.getEventBus().publish(new PlayerReportDeletedEvent(
            EntityMappers.playerReport(report),
            EntityMappers.player(actor)));
      }
    }

    return deleted;
  }

  public boolean isRecentlyReported(PlayerData player, long cooldown) throws SQLException {
    if (cooldown == 0) {
      return false;
    }

    return queryBuilder().where()
        .eq("player_id", player).and()
        .ge("created", (System.currentTimeMillis() / 1000L) - cooldown)
        .countOf() > 0;
  }

  public int deleteById(Integer id) throws SQLException {
    return deleteById(id, null);
  }

  public int deleteById(Integer id, PlayerData actor) throws SQLException {
    PlayerReportData report = queryForId(id);

    if (report == null) return 0;

    super.deleteById(id);

    if (actor != null) {
      plugin.getEventBus().publish(new PlayerReportDeletedEvent(
          EntityMappers.playerReport(report),
          EntityMappers.player(actor)));
    }

    return 1;
  }

  public int deleteIds(Collection<Integer> ids) throws SQLException {
    return deleteIds(ids, null);
  }

  public int deleteIds(Collection<Integer> ids, PlayerData actor) throws SQLException {
    if (ids == null || ids.isEmpty()) return 0;

    List<PlayerReportData> reports = null;
    if (actor != null) {
      reports = new java.util.ArrayList<>(ids.size());
      for (Integer id : ids) {
        PlayerReportData report = queryForId(id);
        if (report != null) reports.add(report);
      }
    }

    DeleteBuilder<PlayerReportData, Integer> builder = deleteBuilder();
    builder.where().in("id", ids);
    int deleted = builder.delete();

    if (deleted > 0 && reports != null) {
      for (PlayerReportData report : reports) {
        plugin.getEventBus().publish(new PlayerReportDeletedEvent(
            EntityMappers.playerReport(report),
            EntityMappers.player(actor)));
      }
    }

    return deleted;
  }

  public long getCount(PlayerData player) throws SQLException {
    return queryBuilder().where().eq("player_id", player).countOf();
  }
}
