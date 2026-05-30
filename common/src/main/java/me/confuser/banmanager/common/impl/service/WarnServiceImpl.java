package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.WarnRequest;
import me.confuser.banmanager.api.service.WarnService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.ormlite.stmt.UpdateBuilder;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class WarnServiceImpl implements WarnService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public WarnServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<PlayerWarn>> warn(WarnRequest request) {
    return async.async(() -> warnSync(request));
  }

  @Override
  public Optional<PlayerWarn> warnSync(WarnRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.player(), "request.player");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData playerEntity = requirePlayer(request.player(), "player");
      PlayerData actorEntity = requirePlayer(request.actor(), "actor");

      PlayerWarnData warn = new PlayerWarnData(
          playerEntity,
          actorEntity,
          request.reason(),
          request.points(),
          request.read(),
          request.expires());

      boolean created = plugin.getPlayerWarnStorage().addWarning(warn, request.silent());
      if (!created) {
        return Optional.<PlayerWarn>empty();
      }

      return Optional.of(EntityMappers.playerWarn(warn));
    });
  }

  @Override
  public CompletableFuture<Page<PlayerWarn>> warnings(UUID player, int page, int size) {
    return async.async(() -> warningsSync(player, page, size));
  }

  @Override
  public Page<PlayerWarn> warningsSync(UUID player, int page, int size) {
    return Pagination.recordsByPlayer(
        plugin.getPlayerWarnStorage(),
        plugin.getPlayerStorage(),
        player,
        page,
        size,
        EntityMappers::playerWarn);
  }

  @Override
  public CompletableFuture<Boolean> markRead(int warnId) {
    return async.async(() -> markReadSync(warnId));
  }

  @Override
  public boolean markReadSync(int warnId) {
    return AsyncSupport.sync(() -> {
      UpdateBuilder<PlayerWarnData, Integer> builder = plugin.getPlayerWarnStorage().updateBuilder();
      builder.updateColumnValue("read", true);
      builder.where().eq("id", warnId);
      return builder.update() > 0;
    }, "Failed to mark warning " + warnId + " read");
  }

  private PlayerData requirePlayer(UUID uuid, String label) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No " + label + " player exists with UUID " + uuid);
    }
    return data;
  }
}
