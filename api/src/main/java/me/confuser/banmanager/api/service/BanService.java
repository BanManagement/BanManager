package me.confuser.banmanager.api.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.PlayerBanRecord;
import me.confuser.banmanager.api.request.BanRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Player ban operations. All mutating methods fire pre-events through the
 * {@link me.confuser.banmanager.api.event.EventBus}; cancelled events
 * resolve to the documented sentinel value ({@link Optional#empty()} for
 * create, {@code false} for delete) on both async and sync variants.
 */
public interface BanService {

  /**
   * Issue a new ban. Fires {@link me.confuser.banmanager.api.event.player.PlayerBanEvent}
   * (cancellable) then {@link me.confuser.banmanager.api.event.player.PlayerBannedEvent}
   * after persistence.
   *
   * @return future resolving to the persisted ban, or empty when cancelled
   */
  CompletableFuture<Optional<PlayerBan>> ban(BanRequest request);

  /**
   * Synchronous variant. Returns {@link Optional#empty()} when a pre-event
   * handler cancels the ban or persistence fails.
   */
  Optional<PlayerBan> banSync(BanRequest request);

  /**
   * Remove the active ban on a player.
   *
   * @return future resolving to {@code true} when an active ban existed and
   *         was removed; {@code false} when no ban existed or a pre-event
   *         handler cancelled the unban
   */
  CompletableFuture<Boolean> unban(UUID player, Player actor, String reason, boolean silent);

  /**
   * Synchronous variant. Returns {@code false} when no ban existed or a
   * pre-event handler cancelled the unban.
   */
  boolean unbanSync(UUID player, Player actor, String reason, boolean silent);

  /**
   * Look up the current ban for a UUID. Backed by an in-memory cache so
   * this is fast and side-effect free.
   */
  Optional<PlayerBan> findActive(UUID player);

  Optional<PlayerBan> findActive(String name);

  boolean isBanned(UUID player);

  boolean isBanned(String name);

  /**
   * Page through historical bans for a player.
   *
   * @param player the player UUID
   * @param page zero-indexed page number
   * @param size page size; 1..200
   */
  CompletableFuture<Page<PlayerBanRecord>> records(UUID player, int page, int size);

  Page<PlayerBanRecord> recordsSync(UUID player, int page, int size);
}
