package me.confuser.banmanager.common.impl.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.IpRangeBanRequest;
import me.confuser.banmanager.api.service.IpRangeBanService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.impl.IpAddressMapper;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class IpRangeBanServiceImpl implements IpRangeBanService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public IpRangeBanServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<IpRangeBan>> ban(IpRangeBanRequest request) {
    return async.async(() -> banSync(request));
  }

  @Override
  public Optional<IpRangeBan> banSync(IpRangeBanRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.fromIp(), "request.fromIp");
    Objects.requireNonNull(request.toIp(), "request.toIp");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData actorEntity = requirePlayer(request.actor());

      IpRangeBanData ban = new IpRangeBanData(
          IpAddressMapper.toInternal(request.fromIp()),
          IpAddressMapper.toInternal(request.toIp()),
          actorEntity,
          request.reason(),
          request.silent(),
          request.expires());

      boolean created = plugin.getIpRangeBanStorage().ban(ban);
      if (!created) {
        return Optional.<IpRangeBan>empty();
      }

      return Optional.of(EntityMappers.ipRangeBan(ban));
    });
  }

  @Override
  public CompletableFuture<Boolean> unban(IpRangeBan ban, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    return async.async(() -> unbanSync(ban, actor, reason, silent));
  }

  @Override
  public boolean unbanSync(IpRangeBan ban, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    Objects.requireNonNull(ban, "ban");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");

    return AsyncSupport.sync(() -> {
      IpRangeBanData internalBan = plugin.getIpRangeBanStorage().getBan(IpAddressMapper.toInternal(ban.fromIp()));
      if (internalBan == null) {
        return false;
      }
      PlayerData actorEntity = requirePlayer(actor.uuid());
      return plugin.getIpRangeBanStorage().unban(internalBan, actorEntity, reason, silent);
    });
  }

  @Override
  public Optional<IpRangeBan> findActive(IPAddress ip) {
    return Optional.ofNullable(EntityMappers.ipRangeBan(plugin.getIpRangeBanStorage().getBan(IpAddressMapper.toInternal(ip))));
  }

  @Override
  public boolean isBanned(IPAddress ip) {
    return plugin.getIpRangeBanStorage().isBanned(IpAddressMapper.toInternal(ip));
  }

  private PlayerData requirePlayer(java.util.UUID uuid) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No actor player exists with UUID " + uuid);
    }
    return data;
  }
}
