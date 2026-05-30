package me.confuser.banmanager.api.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.dto.IpMuteRecord;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.request.IpMuteRequest;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Single-IP mute operations. Cancellation contract matches {@link BanService}:
 * cancelled events resolve to the documented sentinel value
 * ({@link Optional#empty()} for mute, {@code false} for unmute) on both
 * async and sync variants.
 */
public interface IpMuteService {

  CompletableFuture<Optional<IpMute>> mute(IpMuteRequest request);

  /** Returns empty when the pre-event was cancelled or the mute could not be persisted. */
  Optional<IpMute> muteSync(IpMuteRequest request);

  CompletableFuture<Boolean> unmute(IPAddress ip, Player actor, String reason, boolean silent);

  /** Returns {@code false} when no mute existed or the pre-event was cancelled. */
  boolean unmuteSync(IPAddress ip, Player actor, String reason, boolean silent);

  Optional<IpMute> findActive(IPAddress ip);

  boolean isMuted(IPAddress ip);

  CompletableFuture<Page<IpMuteRecord>> records(IPAddress ip, int page, int size);

  Page<IpMuteRecord> recordsSync(IPAddress ip, int page, int size);
}
