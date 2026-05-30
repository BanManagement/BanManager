package me.confuser.banmanager.api.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.request.IpRangeBanRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * IP-range ban operations. Cancellation contract matches {@link BanService}:
 * cancelled events resolve to the documented sentinel value
 * ({@link Optional#empty()} for ban, {@code false} for unban) on both
 * async and sync variants.
 */
public interface IpRangeBanService {

  CompletableFuture<Optional<IpRangeBan>> ban(IpRangeBanRequest request);

  /** Returns empty when the pre-event was cancelled or the ban could not be persisted. */
  Optional<IpRangeBan> banSync(IpRangeBanRequest request);

  CompletableFuture<Boolean> unban(IpRangeBan ban, Player actor, String reason, boolean silent);

  /** Returns {@code false} when no range ban existed or the pre-event was cancelled. */
  boolean unbanSync(IpRangeBan ban, Player actor, String reason, boolean silent);

  /**
   * @return the most-specific active range ban that contains {@code ip},
   *         or empty when no range ban applies
   */
  Optional<IpRangeBan> findActive(IPAddress ip);

  boolean isBanned(IPAddress ip);
}
