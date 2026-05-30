package me.confuser.banmanager.api.service;

import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.request.NameBanRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Name ban operations. Cancellation contract matches {@link BanService}:
 * cancelled events resolve to the documented sentinel value
 * ({@link Optional#empty()} for ban, {@code false} for unban) on both
 * async and sync variants.
 */
public interface NameBanService {

  CompletableFuture<Optional<NameBan>> ban(NameBanRequest request);

  /** Returns empty when the pre-event was cancelled or the ban could not be persisted. */
  Optional<NameBan> banSync(NameBanRequest request);

  CompletableFuture<Boolean> unban(String name, Player actor, String reason, boolean silent);

  /** Returns {@code false} when no name ban existed or the pre-event was cancelled. */
  boolean unbanSync(String name, Player actor, String reason, boolean silent);

  Optional<NameBan> findActive(String name);

  boolean isBanned(String name);
}
