package me.confuser.banmanager.api.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.dto.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Lookup and bookkeeping for known players. All write paths happen via the
 * other services (banning, muting, etc.); this one is read-only plus the
 * {@link #console()} accessor.
 */
public interface PlayerService {

  /**
   * Look up a player by UUID. Future completes empty when the player has
   * never connected.
   */
  CompletableFuture<Optional<Player>> findByUuid(UUID uuid);

  /**
   * Synchronous variant; throws on storage errors.
   */
  Optional<Player> findByUuidSync(UUID uuid);

  /**
   * Look up by name (case-insensitive). Future completes empty when no
   * record exists.
   */
  CompletableFuture<Optional<Player>> findByName(String name);

  Optional<Player> findByNameSync(String name);

  /**
   * @return all players that have logged in from {@code ip} within the
   *         configured "associated alts" window
   */
  CompletableFuture<List<Player>> findByIp(IPAddress ip);

  List<Player> findByIpSync(IPAddress ip);

  /**
   * @return the synthetic "console" actor used for system-issued punishments
   */
  Player console();
}
