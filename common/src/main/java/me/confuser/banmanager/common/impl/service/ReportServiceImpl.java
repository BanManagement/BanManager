package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.dto.ReportState;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.ReportRequest;
import me.confuser.banmanager.api.service.ReportService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ReportServiceImpl implements ReportService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public ReportServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<PlayerReport>> create(ReportRequest request) {
    return async.async(() -> createSync(request));
  }

  @Override
  public Optional<PlayerReport> createSync(ReportRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.player(), "request.player");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData playerEntity = requirePlayer(request.player(), "player");
      PlayerData actorEntity = requirePlayer(request.actor(), "actor");

      me.confuser.banmanager.common.data.ReportState defaultState =
          plugin.getReportStateStorage().queryForId(1);

      PlayerReportData report = new PlayerReportData(playerEntity, actorEntity, request.reason(), defaultState);

      boolean created = plugin.getPlayerReportStorage().report(report, false);
      if (!created) {
        return Optional.<PlayerReport>empty();
      }

      return Optional.of(EntityMappers.playerReport(report));
    });
  }

  @Override
  public CompletableFuture<Boolean> delete(int reportId, Player actor) {
    return async.async(() -> deleteSync(reportId, actor));
  }

  @Override
  public boolean deleteSync(int reportId, Player actor) {
    Objects.requireNonNull(actor, "actor");
    return AsyncSupport.sync(() -> {
      PlayerData actorEntity = requirePlayer(actor.uuid(), "actor");
      return plugin.getPlayerReportStorage().deleteById(reportId, actorEntity) == 1;
    }, "Failed to delete report " + reportId);
  }

  @Override
  public CompletableFuture<Optional<PlayerReport>> findById(int reportId) {
    return async.async(() -> findByIdSync(reportId));
  }

  @Override
  public Optional<PlayerReport> findByIdSync(int reportId) {
    return AsyncSupport.sync(
        () -> Optional.ofNullable(EntityMappers.playerReport(plugin.getPlayerReportStorage().queryForId(reportId))),
        "Failed to load report " + reportId);
  }

  @Override
  public CompletableFuture<Page<PlayerReport>> againstPlayer(UUID player, int page, int size) {
    return async.async(() -> againstPlayerSync(player, page, size));
  }

  @Override
  public Page<PlayerReport> againstPlayerSync(UUID player, int page, int size) {
    return Pagination.recordsByPlayer(
        plugin.getPlayerReportStorage(),
        plugin.getPlayerStorage(),
        player,
        page,
        size,
        EntityMappers::playerReport);
  }

  @Override
  public CompletableFuture<Boolean> updateState(int reportId, ReportState state) {
    return async.async(() -> updateStateSync(reportId, state));
  }

  @Override
  public boolean updateStateSync(int reportId, ReportState state) {
    Objects.requireNonNull(state, "state");
    return AsyncSupport.sync(() -> {
      PlayerReportData report = plugin.getPlayerReportStorage().queryForId(reportId);
      if (report == null) return false;

      me.confuser.banmanager.common.data.ReportState internal =
          plugin.getReportStateStorage().queryForId(state.id());
      if (internal == null) return false;

      report.setState(internal);
      return plugin.getPlayerReportStorage().update(report) == 1;
    }, "Failed to update report " + reportId + " state");
  }

  @Override
  public CompletableFuture<List<ReportState>> states() {
    return async.async(this::statesSync);
  }

  @Override
  public List<ReportState> statesSync() {
    return AsyncSupport.sync(() -> {
      List<me.confuser.banmanager.common.data.ReportState> rows = plugin.getReportStateStorage().queryForAll();
      List<ReportState> mapped = new ArrayList<>(rows.size());
      for (me.confuser.banmanager.common.data.ReportState row : rows) {
        mapped.add(new ReportState(row.getId(), row.getName()));
      }
      return mapped;
    }, "Failed to load report states");
  }

  private PlayerData requirePlayer(UUID uuid, String label) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No " + label + " player exists with UUID " + uuid);
    }
    return data;
  }
}
