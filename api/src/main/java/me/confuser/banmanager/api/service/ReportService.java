package me.confuser.banmanager.api.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.dto.ReportState;
import me.confuser.banmanager.api.request.ReportRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Player report operations. {@code create} fires a cancellable
 * {@link me.confuser.banmanager.api.event.player.PlayerReportEvent}; cancel
 * surfaces as {@link Optional#empty()} on both async and sync paths.
 */
public interface ReportService {

  CompletableFuture<Optional<PlayerReport>> create(ReportRequest request);

  /** Returns empty when the pre-event was cancelled or the report could not be persisted. */
  Optional<PlayerReport> createSync(ReportRequest request);

  CompletableFuture<Boolean> delete(int reportId, Player actor);

  boolean deleteSync(int reportId, Player actor);

  CompletableFuture<Optional<PlayerReport>> findById(int reportId);

  Optional<PlayerReport> findByIdSync(int reportId);

  /**
   * Page through reports filed against {@code player}.
   */
  CompletableFuture<Page<PlayerReport>> againstPlayer(UUID player, int page, int size);

  Page<PlayerReport> againstPlayerSync(UUID player, int page, int size);

  /**
   * Update the workflow state of a report.
   */
  CompletableFuture<Boolean> updateState(int reportId, ReportState state);

  boolean updateStateSync(int reportId, ReportState state);

  /**
   * @return all configured workflow states (e.g. {@code Open}, {@code Assigned},
   *         {@code Resolved})
   */
  CompletableFuture<List<ReportState>> states();

  /**
   * Synchronous variant of {@link #states()} for callers already on a worker
   * thread. Throws {@link me.confuser.banmanager.api.exception.StorageException}
   * if the lookup fails.
   */
  List<ReportState> statesSync();
}
