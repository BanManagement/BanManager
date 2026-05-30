package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.HistoryEntry;
import me.confuser.banmanager.api.dto.PlayerNameSummary;
import me.confuser.banmanager.api.dto.PlayerSession;
import me.confuser.banmanager.api.service.HistoryService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class HistoryServiceImpl implements HistoryService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public HistoryServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Page<HistoryEntry>> history(UUID player, int page, int size) {
    return async.async(() -> historySync(player, page, size));
  }

  @Override
  public Page<HistoryEntry> historySync(UUID player, int page, int size) {
    if (page < 0) throw new IllegalArgumentException("page must be >= 0");
    if (size <= 0 || size > Pagination.MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("size must be in 1.." + Pagination.MAX_PAGE_SIZE);
    }

    return AsyncSupport.sync(() -> {
      PlayerData target = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player));
      if (target == null) {
        return Page.empty(page, size);
      }

      // LIMIT/OFFSET is pushed into the underlying UNION so memory stays
      // bounded regardless of how big the player's history is. The COUNT
      // is a second round-trip, but it's the cost of a real `total` value.
      List<me.confuser.banmanager.common.data.HistoryEntry> rows =
          plugin.getHistoryStorage().getPaged(target, true, true, true, true, true, true, page, size);
      if (rows == null) {
        // HistoryStorage#getPaged contract: never null; defensive guard
        // turns a silent NPE downstream into a clear contract violation.
        throw new IllegalStateException(
            "HistoryStorage.getPaged returned null for player " + player);
      }

      long total = plugin.getHistoryStorage().count(target, true, true, true, true, true, true);

      List<HistoryEntry> slice = new ArrayList<>(rows.size());
      for (me.confuser.banmanager.common.data.HistoryEntry row : rows) {
        slice.add(EntityMappers.historyEntry(row));
      }
      return new Page<>(slice, page, size, total);
    }, "Failed to query history for player " + player);
  }

  @Override
  public CompletableFuture<List<PlayerNameSummary>> names(UUID player) {
    return async.async(() -> namesSync(player));
  }

  @Override
  public List<PlayerNameSummary> namesSync(UUID player) {
    return AsyncSupport.sync(() -> {
      PlayerData target = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player));
      if (target == null) return Collections.<PlayerNameSummary>emptyList();

      List<me.confuser.banmanager.common.data.PlayerNameSummary> rows =
          plugin.getPlayerHistoryStorage().getNamesSummary(target);
      List<PlayerNameSummary> out = new ArrayList<>(rows.size());
      for (me.confuser.banmanager.common.data.PlayerNameSummary row : rows) {
        out.add(new PlayerNameSummary(row.name(), row.firstSeen(), row.lastSeen()));
      }
      return out;
    }, "Failed to query name history for player " + player);
  }

  @Override
  public CompletableFuture<Page<PlayerSession>> sessions(UUID player, long since, int page, int size) {
    return async.async(() -> sessionsSync(player, since, page, size));
  }

  @Override
  public Page<PlayerSession> sessionsSync(UUID player, long since, int page, int size) {
    if (page < 0) throw new IllegalArgumentException("page must be >= 0");
    if (size <= 0 || size > Pagination.MAX_PAGE_SIZE) {
      throw new IllegalArgumentException("size must be in 1.." + Pagination.MAX_PAGE_SIZE);
    }

    return AsyncSupport.sync(() -> {
      PlayerData target = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player));
      if (target == null) return Page.<PlayerSession>empty(page, size);

      List<PlayerSession> sessions = new ArrayList<>();
      /*
       * try-with-resources is safe here because we're inside SqlCallable.call(),
       * which already declares `throws Exception`; that lets close()
       * propagate without forcing the outer service method to declare
       * any checked exception.
       */
      try (CloseableIterator<PlayerHistoryData> it =
               plugin.getPlayerHistoryStorage().getSince(target, since, page, size)) {
        while (it.hasNext()) {
          sessions.add(EntityMappers.playerSession(it.next()));
        }
      }
      long total = plugin.getPlayerHistoryStorage().countSince(target, since);
      return new Page<>(sessions, page, size, total);
    }, "Failed to query sessions for player " + player);
  }

  @Override
  public CompletableFuture<Optional<String>> nameAt(UUID player, long timestamp) {
    return async.async(() -> nameAtSync(player, timestamp));
  }

  @Override
  public Optional<String> nameAtSync(UUID player, long timestamp) {
    return AsyncSupport.sync(() -> {
      PlayerData target = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(player));
      if (target == null) return Optional.<String>empty();
      return Optional.ofNullable(plugin.getPlayerHistoryStorage().getNameAt(target, timestamp));
    }, "Failed to query name at " + timestamp + " for player " + player);
  }

}
