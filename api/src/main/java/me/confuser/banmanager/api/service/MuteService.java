package me.confuser.banmanager.api.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.dto.PlayerMuteRecord;
import me.confuser.banmanager.api.request.MuteRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Player mute operations. Cancellation contract matches {@link BanService}:
 * cancelled events resolve to the documented sentinel value
 * ({@link Optional#empty()} for mute, {@code false} for unmute) on both
 * async and sync variants.
 */
public interface MuteService {

  CompletableFuture<Optional<PlayerMute>> mute(MuteRequest request);

  /** Returns empty when the pre-event was cancelled or the mute could not be persisted. */
  Optional<PlayerMute> muteSync(MuteRequest request);

  CompletableFuture<Boolean> unmute(UUID player, Player actor, String reason, boolean silent);

  /** Returns {@code false} when no mute existed or the pre-event was cancelled. */
  boolean unmuteSync(UUID player, Player actor, String reason, boolean silent);

  Optional<PlayerMute> findActive(UUID player);

  Optional<PlayerMute> findActive(String name);

  boolean isMuted(UUID player);

  boolean isMuted(String name);

  CompletableFuture<Page<PlayerMuteRecord>> records(UUID player, int page, int size);

  Page<PlayerMuteRecord> recordsSync(UUID player, int page, int size);
}
