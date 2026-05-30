package me.confuser.banmanager.api.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.api.request.WarnRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface WarnService {

  /**
   * Issue a warning. Fires {@link me.confuser.banmanager.api.event.player.PlayerWarnEvent}
   * (now cancellable on every platform) and
   * {@link me.confuser.banmanager.api.event.player.PlayerWarnedEvent}.
   *
   * @return future resolving to the persisted warning, or empty when
   *         cancelled by a pre-event handler
   */
  CompletableFuture<Optional<PlayerWarn>> warn(WarnRequest request);

  /** Returns empty when the pre-event was cancelled or the warning could not be persisted. */
  Optional<PlayerWarn> warnSync(WarnRequest request);

  /**
   * @return list of unread warnings for the player (warnings displayed at
   *         next login)
   */
  CompletableFuture<Page<PlayerWarn>> warnings(UUID player, int page, int size);

  Page<PlayerWarn> warningsSync(UUID player, int page, int size);

  /**
   * Mark a warning as read.
   */
  CompletableFuture<Boolean> markRead(int warnId);

  boolean markReadSync(int warnId);
}
