package me.confuser.banmanager.common.impl.service;

import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.NameBanRequest;
import me.confuser.banmanager.api.service.NameBanService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class NameBanServiceImpl implements NameBanService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public NameBanServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<NameBan>> ban(NameBanRequest request) {
    return async.async(() -> banSync(request));
  }

  @Override
  public Optional<NameBan> banSync(NameBanRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.name(), "request.name");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData actorEntity = requirePlayer(request.actor());

      NameBanData ban = new NameBanData(
          request.name(),
          actorEntity,
          request.reason(),
          request.silent(),
          request.expires());

      boolean created = plugin.getNameBanStorage().ban(ban);
      if (!created) {
        return Optional.<NameBan>empty();
      }

      return Optional.of(EntityMappers.nameBan(ban));
    });
  }

  @Override
  public CompletableFuture<Boolean> unban(String name, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    return async.async(() -> unbanSync(name, actor, reason, silent));
  }

  @Override
  public boolean unbanSync(String name, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");

    return AsyncSupport.sync(() -> {
      NameBanData ban = plugin.getNameBanStorage().getBan(name);
      if (ban == null) {
        return false;
      }
      PlayerData actorEntity = requirePlayer(actor.uuid());
      return plugin.getNameBanStorage().unban(ban, actorEntity, reason, false, silent);
    });
  }

  @Override
  public Optional<NameBan> findActive(String name) {
    return Optional.ofNullable(EntityMappers.nameBan(plugin.getNameBanStorage().getBan(name)));
  }

  @Override
  public boolean isBanned(String name) {
    return plugin.getNameBanStorage().isBanned(name);
  }

  private PlayerData requirePlayer(java.util.UUID uuid) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No actor player exists with UUID " + uuid);
    }
    return data;
  }
}
