package me.confuser.banmanager.common.impl.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.service.PlayerService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.impl.IpAddressMapper;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Default {@link PlayerService} implementation. Delegates to
 * {@link me.confuser.banmanager.common.storage.PlayerStorage} and translates
 * results into API DTOs.
 *
 * <p>Lookups are read-only — write paths happen through the punishment
 * services which create {@code PlayerData} on the fly via
 * {@code createIfNotExists}. Exposing those primitives here would tempt
 * callers into bypassing the event bus.</p>
 */
public final class PlayerServiceImpl implements PlayerService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public PlayerServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<Player>> findByUuid(UUID uuid) {
    return async.async(() -> findByUuidSync(uuid));
  }

  @Override
  public Optional<Player> findByUuidSync(UUID uuid) {
    return AsyncSupport.sync(() -> {
      PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
      return Optional.ofNullable(EntityMappers.player(data));
    });
  }

  @Override
  public CompletableFuture<Optional<Player>> findByName(String name) {
    return async.async(() -> findByNameSync(name));
  }

  @Override
  public Optional<Player> findByNameSync(String name) {
    return AsyncSupport.sync(() -> {
      PlayerData data = plugin.getPlayerStorage().findByExactName(name);
      return Optional.ofNullable(EntityMappers.player(data));
    }, "Failed to look up player by name " + name);
  }

  @Override
  public CompletableFuture<List<Player>> findByIp(IPAddress ip) {
    return async.async(() -> findByIpSync(ip));
  }

  @Override
  public List<Player> findByIpSync(IPAddress ip) {
    me.confuser.banmanager.common.ipaddr.IPAddress internal = IpAddressMapper.toInternal(ip);
    return AsyncSupport.sync(() -> {
      List<PlayerData> matches = plugin.getPlayerStorage().findDuplicatesInTime(internal, 0L);
      List<Player> result = new ArrayList<>(matches.size());
      for (PlayerData data : matches) {
        result.add(EntityMappers.player(data));
      }
      return result;
    }, "Failed to look up players by IP " + ip);
  }

  @Override
  public Player console() {
    return EntityMappers.player(plugin.getPlayerStorage().getConsole());
  }
}
