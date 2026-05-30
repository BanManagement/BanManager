package me.confuser.banmanager.common.impl.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.dto.IpMuteRecord;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.IpMuteRequest;
import me.confuser.banmanager.api.service.IpMuteService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.impl.IpAddressMapper;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class IpMuteServiceImpl implements IpMuteService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public IpMuteServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<IpMute>> mute(IpMuteRequest request) {
    return async.async(() -> muteSync(request));
  }

  @Override
  public Optional<IpMute> muteSync(IpMuteRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.ip(), "request.ip");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData actorEntity = requirePlayer(request.actor());

      IpMuteData mute = new IpMuteData(
          IpAddressMapper.toInternal(request.ip()),
          actorEntity,
          request.reason(),
          request.silent(),
          request.soft(),
          request.expires());

      boolean created = plugin.getIpMuteStorage().mute(mute);
      if (!created) {
        return Optional.<IpMute>empty();
      }

      return Optional.of(EntityMappers.ipMute(mute));
    });
  }

  @Override
  public CompletableFuture<Boolean> unmute(IPAddress ip, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    return async.async(() -> unmuteSync(ip, actor, reason, silent));
  }

  @Override
  public boolean unmuteSync(IPAddress ip, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    Objects.requireNonNull(ip, "ip");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");

    return AsyncSupport.sync(() -> {
      IpMuteData mute = plugin.getIpMuteStorage().getMute(IpAddressMapper.toInternal(ip));
      if (mute == null) {
        return false;
      }
      PlayerData actorEntity = requirePlayer(actor.uuid());
      return plugin.getIpMuteStorage().unmute(mute, actorEntity, reason, silent);
    });
  }

  @Override
  public Optional<IpMute> findActive(IPAddress ip) {
    return Optional.ofNullable(EntityMappers.ipMute(plugin.getIpMuteStorage().getMute(IpAddressMapper.toInternal(ip))));
  }

  @Override
  public boolean isMuted(IPAddress ip) {
    return plugin.getIpMuteStorage().isMuted(IpAddressMapper.toInternal(ip));
  }

  @Override
  public CompletableFuture<Page<IpMuteRecord>> records(IPAddress ip, int page, int size) {
    return async.async(() -> recordsSync(ip, page, size));
  }

  @Override
  public Page<IpMuteRecord> recordsSync(IPAddress ip, int page, int size) {
    return Pagination.recordsByColumn(
        plugin.getIpMuteRecordStorage(),
        "ip",
        IpAddressMapper.toInternal(ip),
        page,
        size,
        EntityMappers::ipMuteRecord);
  }

  private PlayerData requirePlayer(java.util.UUID uuid) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No actor player exists with UUID " + uuid);
    }
    return data;
  }
}
