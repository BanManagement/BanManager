package me.confuser.banmanager.api.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.HistoryEntry;
import me.confuser.banmanager.api.dto.PlayerNameSummary;
import me.confuser.banmanager.api.dto.PlayerSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Read-only access to player session and combined punishment history.
 */
public interface HistoryService {

  /**
   * @return cross-table history (bans, mutes, warnings, kicks, notes) for a
   *         player, newest first
   */
  CompletableFuture<Page<HistoryEntry>> history(UUID player, int page, int size);

  Page<HistoryEntry> historySync(UUID player, int page, int size);

  /**
   * @return all known names this player has used, with first/last seen
   *         timestamps; ordered most-recent first
   */
  CompletableFuture<List<PlayerNameSummary>> names(UUID player);

  List<PlayerNameSummary> namesSync(UUID player);

  /**
   * @return paginated list of login sessions since {@code since} unix
   *         seconds, newest first
   */
  CompletableFuture<Page<PlayerSession>> sessions(UUID player, long since, int page, int size);

  Page<PlayerSession> sessionsSync(UUID player, long since, int page, int size);

  /**
   * @return the name in use by the player at the given timestamp
   */
  CompletableFuture<Optional<String>> nameAt(UUID player, long timestamp);

  Optional<String> nameAtSync(UUID player, long timestamp);
}
