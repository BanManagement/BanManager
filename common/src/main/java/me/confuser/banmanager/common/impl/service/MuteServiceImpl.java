package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.dto.PlayerMuteRecord;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.MuteRequest;
import me.confuser.banmanager.api.service.MuteService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MuteServiceImpl implements MuteService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public MuteServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<PlayerMute>> mute(MuteRequest request) {
    return async.async(() -> muteSync(request));
  }

  @Override
  public Optional<PlayerMute> muteSync(MuteRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.player(), "request.player");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData playerEntity = requirePlayer(request.player(), "player");
      PlayerData actorEntity = requirePlayer(request.actor(), "actor");

      PlayerMuteData mute = new PlayerMuteData(
          playerEntity,
          actorEntity,
          request.reason(),
          request.silent(),
          request.soft(),
          request.expires(),
          request.onlineOnly());

      boolean created = plugin.getPlayerMuteStorage().mute(mute);
      if (!created) {
        return Optional.<PlayerMute>empty();
      }

      return Optional.of(EntityMappers.playerMute(mute));
    });
  }

  @Override
  public CompletableFuture<Boolean> unmute(UUID player, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    return async.async(() -> unmuteSync(player, actor, reason, silent));
  }

  @Override
  public boolean unmuteSync(UUID player, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");

    return AsyncSupport.sync(() -> {
      PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player);
      if (mute == null) {
        return false;
      }
      PlayerData actorEntity = requirePlayer(actor.uuid(), "actor");
      return plugin.getPlayerMuteStorage().unmute(mute, actorEntity, reason, false, silent);
    });
  }

  @Override
  public Optional<PlayerMute> findActive(UUID player) {
    return Optional.ofNullable(EntityMappers.playerMute(plugin.getPlayerMuteStorage().getMute(player)));
  }

  @Override
  public Optional<PlayerMute> findActive(String name) {
    return Optional.ofNullable(EntityMappers.playerMute(plugin.getPlayerMuteStorage().getMute(name)));
  }

  @Override
  public boolean isMuted(UUID player) {
    return plugin.getPlayerMuteStorage().isMuted(player);
  }

  @Override
  public boolean isMuted(String name) {
    return plugin.getPlayerMuteStorage().isMuted(name);
  }

  @Override
  public CompletableFuture<Page<PlayerMuteRecord>> records(UUID player, int page, int size) {
    return async.async(() -> recordsSync(player, page, size));
  }

  @Override
  public Page<PlayerMuteRecord> recordsSync(UUID player, int page, int size) {
    return Pagination.recordsByPlayer(
        plugin.getPlayerMuteRecordStorage(),
        plugin.getPlayerStorage(),
        player,
        page,
        size,
        EntityMappers::playerMuteRecord);
  }

  private PlayerData requirePlayer(UUID uuid, String label) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No " + label + " player exists with UUID " + uuid);
    }
    return data;
  }
}
