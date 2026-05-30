package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.PlayerBanRecord;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.BanRequest;
import me.confuser.banmanager.api.service.BanService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Default {@link BanService}. Thin wrapper over {@code PlayerBanStorage} which
 * owns the canonical event publishing for ban operations. Pre-event cancellation
 * surfaces here as {@link Optional#empty()} on {@link #ban(BanRequest)} and
 * {@link Boolean#FALSE} on {@link #unban}.
 */
public final class BanServiceImpl implements BanService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public BanServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<PlayerBan>> ban(BanRequest request) {
    return async.async(() -> banSync(request));
  }

  @Override
  public Optional<PlayerBan> banSync(BanRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.player(), "request.player");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData playerEntity = requirePlayer(request.player(), "player");
      PlayerData actorEntity = requirePlayer(request.actor(), "actor");

      PlayerBanData ban = new PlayerBanData(
          playerEntity,
          actorEntity,
          request.reason(),
          request.silent(),
          request.expires());

      boolean created = plugin.getPlayerBanStorage().ban(ban);
      if (!created) {
        return Optional.<PlayerBan>empty();
      }

      return Optional.of(EntityMappers.playerBan(ban));
    });
  }

  @Override
  public CompletableFuture<Boolean> unban(UUID player, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    return async.async(() -> unbanSync(player, actor, reason, silent));
  }

  @Override
  public boolean unbanSync(UUID player, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");

    return AsyncSupport.sync(() -> {
      PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player);
      if (ban == null) {
        return false;
      }
      PlayerData actorEntity = requirePlayer(actor.uuid(), "actor");
      return plugin.getPlayerBanStorage().unban(ban, actorEntity, reason, false, silent);
    });
  }

  @Override
  public Optional<PlayerBan> findActive(UUID player) {
    return Optional.ofNullable(EntityMappers.playerBan(plugin.getPlayerBanStorage().getBan(player)));
  }

  @Override
  public Optional<PlayerBan> findActive(String name) {
    return Optional.ofNullable(EntityMappers.playerBan(plugin.getPlayerBanStorage().getBan(name)));
  }

  @Override
  public boolean isBanned(UUID player) {
    return plugin.getPlayerBanStorage().isBanned(player);
  }

  @Override
  public boolean isBanned(String name) {
    return plugin.getPlayerBanStorage().isBanned(name);
  }

  @Override
  public CompletableFuture<Page<PlayerBanRecord>> records(UUID player, int page, int size) {
    return async.async(() -> recordsSync(player, page, size));
  }

  @Override
  public Page<PlayerBanRecord> recordsSync(UUID player, int page, int size) {
    return Pagination.recordsByPlayer(
        plugin.getPlayerBanRecordStorage(),
        plugin.getPlayerStorage(),
        player,
        page,
        size,
        EntityMappers::playerBanRecord);
  }

  private PlayerData requirePlayer(UUID uuid, String label) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No " + label + " player exists with UUID " + uuid);
    }
    return data;
  }
}
