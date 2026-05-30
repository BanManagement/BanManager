package me.confuser.banmanager.common.impl.service;

import inet.ipaddr.IPAddress;
import me.confuser.banmanager.api.Page;
import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.IpBanRecord;
import me.confuser.banmanager.api.exception.EntityNotFoundException;
import me.confuser.banmanager.api.request.IpBanRequest;
import me.confuser.banmanager.api.service.IpBanService;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.impl.AsyncSupport;
import me.confuser.banmanager.common.impl.EntityMappers;
import me.confuser.banmanager.common.impl.IpAddressMapper;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class IpBanServiceImpl implements IpBanService {

  private final BanManagerPlugin plugin;
  private final AsyncSupport async;

  public IpBanServiceImpl(BanManagerPlugin plugin, AsyncSupport async) {
    this.plugin = plugin;
    this.async = async;
  }

  @Override
  public CompletableFuture<Optional<IpBan>> ban(IpBanRequest request) {
    return async.async(() -> banSync(request));
  }

  @Override
  public Optional<IpBan> banSync(IpBanRequest request) {
    Objects.requireNonNull(request, "request");
    Objects.requireNonNull(request.ip(), "request.ip");
    Objects.requireNonNull(request.actor(), "request.actor");
    Objects.requireNonNull(request.reason(), "request.reason");

    return AsyncSupport.sync(() -> {
      PlayerData actorEntity = requirePlayer(request.actor());

      IpBanData ban = new IpBanData(
          IpAddressMapper.toInternal(request.ip()),
          actorEntity,
          request.reason(),
          request.silent(),
          request.expires());

      boolean created = plugin.getIpBanStorage().ban(ban);
      if (!created) {
        return Optional.<IpBan>empty();
      }

      return Optional.of(EntityMappers.ipBan(ban));
    });
  }

  @Override
  public CompletableFuture<Boolean> unban(IPAddress ip, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    return async.async(() -> unbanSync(ip, actor, reason, silent));
  }

  @Override
  public boolean unbanSync(IPAddress ip, me.confuser.banmanager.api.dto.Player actor, String reason, boolean silent) {
    Objects.requireNonNull(ip, "ip");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");

    return AsyncSupport.sync(() -> {
      IpBanData ban = plugin.getIpBanStorage().getBan(IpAddressMapper.toInternal(ip));
      if (ban == null) {
        return false;
      }
      PlayerData actorEntity = requirePlayer(actor.uuid());
      return plugin.getIpBanStorage().unban(ban, actorEntity, reason, false, silent);
    });
  }

  @Override
  public Optional<IpBan> findActive(IPAddress ip) {
    return Optional.ofNullable(EntityMappers.ipBan(plugin.getIpBanStorage().getBan(IpAddressMapper.toInternal(ip))));
  }

  @Override
  public boolean isBanned(IPAddress ip) {
    return plugin.getIpBanStorage().isBanned(IpAddressMapper.toInternal(ip));
  }

  @Override
  public CompletableFuture<Page<IpBanRecord>> records(IPAddress ip, int page, int size) {
    return async.async(() -> recordsSync(ip, page, size));
  }

  @Override
  public Page<IpBanRecord> recordsSync(IPAddress ip, int page, int size) {
    return Pagination.recordsByColumn(
        plugin.getIpBanRecordStorage(),
        "ip",
        IpAddressMapper.toInternal(ip),
        page,
        size,
        EntityMappers::ipBanRecord);
  }

  private PlayerData requirePlayer(java.util.UUID uuid) throws Exception {
    PlayerData data = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(uuid));
    if (data == null) {
      throw new EntityNotFoundException("No actor player exists with UUID " + uuid);
    }
    return data;
  }
}
