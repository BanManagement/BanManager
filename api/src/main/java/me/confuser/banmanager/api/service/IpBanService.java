package me.confuser.banmanager.api.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.IpBanRecord;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.request.IpBanRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Single-IP ban operations. Cancellation contract matches {@link BanService}:
 * cancelled events resolve to the documented sentinel value
 * ({@link Optional#empty()} for ban, {@code false} for unban) on both
 * async and sync variants.
 */
public interface IpBanService {

  CompletableFuture<Optional<IpBan>> ban(IpBanRequest request);

  /** Returns empty when the pre-event was cancelled or the ban could not be persisted. */
  Optional<IpBan> banSync(IpBanRequest request);

  CompletableFuture<Boolean> unban(IPAddress ip, Player actor, String reason, boolean silent);

  /** Returns {@code false} when no ban existed or the pre-event was cancelled. */
  boolean unbanSync(IPAddress ip, Player actor, String reason, boolean silent);

  Optional<IpBan> findActive(IPAddress ip);

  boolean isBanned(IPAddress ip);

  CompletableFuture<Page<IpBanRecord>> records(IPAddress ip, int page, int size);

  Page<IpBanRecord> recordsSync(IPAddress ip, int page, int size);
}
