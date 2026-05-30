package me.confuser.banmanager.common.impl;

import me.confuser.banmanager.api.dto.HistoryEntry;
import me.confuser.banmanager.api.dto.IpBan;
import me.confuser.banmanager.api.dto.IpBanRecord;
import me.confuser.banmanager.api.dto.IpMute;
import me.confuser.banmanager.api.dto.IpMuteRecord;
import me.confuser.banmanager.api.dto.IpRangeBan;
import me.confuser.banmanager.api.dto.IpRangeBanRecord;
import me.confuser.banmanager.api.dto.NameBan;
import me.confuser.banmanager.api.dto.NameBanRecord;
import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerBan;
import me.confuser.banmanager.api.dto.PlayerBanRecord;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.dto.PlayerMuteRecord;
import me.confuser.banmanager.api.dto.PlayerNameSummary;
import me.confuser.banmanager.api.dto.PlayerNote;
import me.confuser.banmanager.api.dto.PlayerReport;
import me.confuser.banmanager.api.dto.PlayerSession;
import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.api.dto.ReportState;
import me.confuser.banmanager.api.request.BanRequest;
import me.confuser.banmanager.api.request.IpBanRequest;
import me.confuser.banmanager.api.request.IpMuteRequest;
import me.confuser.banmanager.api.request.IpRangeBanRequest;
import me.confuser.banmanager.api.request.MuteRequest;
import me.confuser.banmanager.api.request.NameBanRequest;
import me.confuser.banmanager.api.request.NoteRequest;
import me.confuser.banmanager.api.request.ReportRequest;
import me.confuser.banmanager.api.request.WarnRequest;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerHistoryData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerWarnData;

import java.util.Optional;

/**
 * Static mapping helpers between internal ORMLite-annotated entities and the
 * public API surface (immutable record DTOs and mutable {@code Request} POJOs).
 *
 * <p>Three groups of mappers:</p>
 * <ul>
 *   <li>{@code entity → DTO} for post-event payloads and read APIs.</li>
 *   <li>{@code entity → Request} for pre-event payloads — handlers receive a
 *       request snapshot they can mutate (reason/expires/silent/etc.) before
 *       persistence.</li>
 *   <li>{@link #applyTo(BanRequest, PlayerBanData) applyTo(...)} families that
 *       copy the (possibly mutated) Request fields back onto the entity right
 *       before {@code create(...)} runs, so handler edits actually take effect.</li>
 * </ul>
 *
 * <p>{@code Player}/IP/timestamp identity fields are <em>not</em> copied back —
 * they are part of the entity's identity and are validated at the service
 * boundary, not by the storage layer.</p>
 *
 * <p>All entity → DTO/Request mappers tolerate {@code null} input, returning
 * {@code null} so callers can chain {@code map(x)} on optional fields without
 * explicit null checks.</p>
 */
public final class EntityMappers {

  private EntityMappers() {}

  public static Player player(PlayerData data) {
    if (data == null) return null;
    return new Player(
        data.getUUID(),
        data.getName(),
        IpAddressMapper.toApi(data.getIp()),
        data.getLastSeen(),
        Optional.ofNullable(data.getLocale()).filter(s -> !s.isEmpty()));
  }

  public static PlayerBan playerBan(PlayerBanData data) {
    if (data == null) return null;
    return new PlayerBan(
        data.getId(),
        player(data.getPlayer()),
        player(data.getActor()),
        data.getReason(),
        data.getCreated(),
        data.getUpdated(),
        data.getExpires(),
        data.isSilent());
  }

  public static PlayerBanRecord playerBanRecord(me.confuser.banmanager.common.data.PlayerBanRecord internal) {
    if (internal == null) return null;
    return new PlayerBanRecord(
        internal.getId(),
        player(internal.getPlayer()),
        player(internal.getActor()),
        player(internal.getPastActor()),
        internal.getReason(),
        emptyIfNull(internal.getCreatedReason()),
        internal.getExpired(),
        internal.getPastCreated(),
        internal.getCreated(),
        internal.isSilent());
  }

  public static PlayerMute playerMute(PlayerMuteData data) {
    if (data == null) return null;
    return new PlayerMute(
        data.getId(),
        player(data.getPlayer()),
        player(data.getActor()),
        data.getReason(),
        data.getCreated(),
        data.getUpdated(),
        data.getExpires(),
        data.isSoft(),
        data.isSilent(),
        data.isOnlineOnly(),
        data.getPausedRemaining());
  }

  public static PlayerMuteRecord playerMuteRecord(me.confuser.banmanager.common.data.PlayerMuteRecord internal) {
    if (internal == null) return null;
    return new PlayerMuteRecord(
        internal.getId(),
        player(internal.getPlayer()),
        player(internal.getActor()),
        player(internal.getPastActor()),
        internal.getReason(),
        emptyIfNull(internal.getCreatedReason()),
        internal.getExpired(),
        internal.getPastCreated(),
        internal.getCreated(),
        internal.isSoft(),
        internal.isSilent(),
        internal.isOnlineOnly(),
        internal.getRemainingOnlineTime());
  }

  public static PlayerWarn playerWarn(PlayerWarnData data) {
    if (data == null) return null;
    return new PlayerWarn(
        data.getId(),
        player(data.getPlayer()),
        player(data.getActor()),
        data.getReason(),
        data.getPoints(),
        data.isRead(),
        data.getCreated(),
        data.getExpires());
  }

  public static IpBan ipBan(IpBanData data) {
    if (data == null) return null;
    return new IpBan(
        data.getId(),
        IpAddressMapper.toApi(data.getIp()),
        player(data.getActor()),
        data.getReason(),
        data.getCreated(),
        data.getUpdated(),
        data.getExpires(),
        data.isSilent());
  }

  public static IpBanRecord ipBanRecord(me.confuser.banmanager.common.data.IpBanRecord internal) {
    if (internal == null) return null;
    return new IpBanRecord(
        internal.getId(),
        IpAddressMapper.toApi(internal.getIp()),
        player(internal.getActor()),
        player(internal.getPastActor()),
        internal.getReason(),
        emptyIfNull(internal.getCreatedReason()),
        internal.getExpired(),
        internal.getPastCreated(),
        internal.getCreated(),
        internal.isSilent());
  }

  public static IpMute ipMute(IpMuteData data) {
    if (data == null) return null;
    return new IpMute(
        data.getId(),
        IpAddressMapper.toApi(data.getIp()),
        player(data.getActor()),
        data.getReason(),
        data.getCreated(),
        data.getUpdated(),
        data.getExpires(),
        data.isSoft(),
        data.isSilent());
  }

  public static IpMuteRecord ipMuteRecord(me.confuser.banmanager.common.data.IpMuteRecord internal) {
    if (internal == null) return null;
    return new IpMuteRecord(
        internal.getId(),
        IpAddressMapper.toApi(internal.getIp()),
        player(internal.getActor()),
        player(internal.getPastActor()),
        internal.getReason(),
        emptyIfNull(internal.getCreatedReason()),
        internal.getExpired(),
        internal.getPastCreated(),
        internal.getCreated(),
        internal.isSoft(),
        internal.isSilent());
  }

  public static IpRangeBan ipRangeBan(IpRangeBanData data) {
    if (data == null) return null;
    return new IpRangeBan(
        data.getId(),
        IpAddressMapper.toApi(data.getFromIp()),
        IpAddressMapper.toApi(data.getToIp()),
        player(data.getActor()),
        data.getReason(),
        data.getCreated(),
        data.getUpdated(),
        data.getExpires(),
        data.isSilent());
  }

  public static IpRangeBanRecord ipRangeBanRecord(me.confuser.banmanager.common.data.IpRangeBanRecord internal) {
    if (internal == null) return null;
    return new IpRangeBanRecord(
        internal.getId(),
        IpAddressMapper.toApi(internal.getFromIp()),
        IpAddressMapper.toApi(internal.getToIp()),
        player(internal.getActor()),
        player(internal.getPastActor()),
        internal.getReason(),
        emptyIfNull(internal.getCreatedReason()),
        internal.getExpired(),
        internal.getPastCreated(),
        internal.getCreated(),
        internal.isSilent());
  }

  public static NameBan nameBan(NameBanData data) {
    if (data == null) return null;
    return new NameBan(
        data.getId(),
        data.getName(),
        player(data.getActor()),
        data.getReason(),
        data.getCreated(),
        data.getUpdated(),
        data.getExpires(),
        data.isSilent());
  }

  public static NameBanRecord nameBanRecord(me.confuser.banmanager.common.data.NameBanRecord internal) {
    if (internal == null) return null;
    return new NameBanRecord(
        internal.getId(),
        internal.getName(),
        player(internal.getActor()),
        player(internal.getPastActor()),
        internal.getReason(),
        emptyIfNull(internal.getCreatedReason()),
        internal.getExpired(),
        internal.getPastCreated(),
        internal.getCreated(),
        internal.isSilent());
  }

  public static PlayerNote playerNote(PlayerNoteData data) {
    if (data == null) return null;
    return new PlayerNote(
        data.getId(),
        player(data.getPlayer()),
        player(data.getActor()),
        data.getMessage(),
        data.getCreated());
  }

  public static PlayerReport playerReport(PlayerReportData data) {
    if (data == null) return null;
    return new PlayerReport(
        data.getId(),
        player(data.getPlayer()),
        player(data.getActor()),
        Optional.ofNullable(player(data.getAssignee())),
        reportState(data.getState()),
        data.getReason(),
        data.getCreated(),
        data.getUpdated());
  }

  public static ReportState reportState(me.confuser.banmanager.common.data.ReportState data) {
    if (data == null) return null;
    return new ReportState(data.getId(), data.getName());
  }

  public static PlayerSession playerSession(PlayerHistoryData data) {
    if (data == null) return null;
    return new PlayerSession(
        data.getId(),
        player(data.getPlayer()),
        data.getName(),
        Optional.ofNullable(IpAddressMapper.toApi(data.getIp())),
        data.getJoin(),
        data.getLeave());
  }

  public static HistoryEntry historyEntry(me.confuser.banmanager.common.data.HistoryEntry internal) {
    if (internal == null) return null;
    return new HistoryEntry(
        internal.id(),
        internal.type(),
        internal.actor(),
        internal.created(),
        emptyIfNull(internal.reason()),
        emptyIfNull(internal.meta()));
  }

  public static PlayerNameSummary playerNameSummary(me.confuser.banmanager.common.data.PlayerNameSummary internal) {
    if (internal == null) return null;
    return new PlayerNameSummary(internal.name(), internal.firstSeen(), internal.lastSeen());
  }

  // ---------------------------------------------------------------------
  // Entity → Request mappers (snapshot of the about-to-persist entity, used
  // as the pre-event payload so handlers see the entity's current state and
  // can mutate fields like reason/expires/silent before persistence).
  // ---------------------------------------------------------------------

  public static BanRequest banRequest(PlayerBanData data) {
    if (data == null) return null;
    BanRequest req = new BanRequest()
        .player(data.getPlayer().getUUID())
        .actor(data.getActor().getUUID())
        .reason(data.getReason())
        .expires(data.getExpires())
        .silent(data.isSilent());
    return req;
  }

  public static MuteRequest muteRequest(PlayerMuteData data) {
    if (data == null) return null;
    return new MuteRequest()
        .player(data.getPlayer().getUUID())
        .actor(data.getActor().getUUID())
        .reason(data.getReason())
        .expires(data.getExpires())
        .soft(data.isSoft())
        .silent(data.isSilent())
        .onlineOnly(data.isOnlineOnly());
  }

  public static WarnRequest warnRequest(PlayerWarnData data, boolean silent) {
    if (data == null) return null;
    return new WarnRequest()
        .player(data.getPlayer().getUUID())
        .actor(data.getActor().getUUID())
        .reason(data.getReason())
        .points(data.getPoints())
        .read(data.isRead())
        .expires(data.getExpires())
        .silent(silent);
  }

  public static NoteRequest noteRequest(PlayerNoteData data) {
    if (data == null) return null;
    return new NoteRequest()
        .player(data.getPlayer().getUUID())
        .actor(data.getActor().getUUID())
        .message(data.getMessage());
  }

  public static ReportRequest reportRequest(PlayerReportData data) {
    if (data == null) return null;
    return new ReportRequest()
        .player(data.getPlayer().getUUID())
        .actor(data.getActor().getUUID())
        .reason(data.getReason());
  }

  public static IpBanRequest ipBanRequest(IpBanData data) {
    if (data == null) return null;
    return new IpBanRequest()
        .ip(IpAddressMapper.toApi(data.getIp()))
        .actor(data.getActor().getUUID())
        .reason(data.getReason())
        .expires(data.getExpires())
        .silent(data.isSilent());
  }

  public static IpMuteRequest ipMuteRequest(IpMuteData data) {
    if (data == null) return null;
    return new IpMuteRequest()
        .ip(IpAddressMapper.toApi(data.getIp()))
        .actor(data.getActor().getUUID())
        .reason(data.getReason())
        .expires(data.getExpires())
        .soft(data.isSoft())
        .silent(data.isSilent());
  }

  public static IpRangeBanRequest ipRangeBanRequest(IpRangeBanData data) {
    if (data == null) return null;
    return new IpRangeBanRequest()
        .fromIp(IpAddressMapper.toApi(data.getFromIp()))
        .toIp(IpAddressMapper.toApi(data.getToIp()))
        .actor(data.getActor().getUUID())
        .reason(data.getReason())
        .expires(data.getExpires())
        .silent(data.isSilent());
  }

  public static NameBanRequest nameBanRequest(NameBanData data) {
    if (data == null) return null;
    return new NameBanRequest()
        .name(data.getName())
        .actor(data.getActor().getUUID())
        .reason(data.getReason())
        .expires(data.getExpires())
        .silent(data.isSilent());
  }

  // ---------------------------------------------------------------------
  // Request → Entity mutators. Called *after* the pre-event has fired so the
  // entity's reason/expires/silent (etc.) reflect any handler edits before we
  // persist. Identity fields (player, actor, ip, timestamps) are validated at
  // the service boundary and never copied back here.
  // ---------------------------------------------------------------------

  public static void applyTo(BanRequest req, PlayerBanData target) {
    target.setReason(req.reason());
    target.setExpires(req.expires());
    target.setSilent(req.silent());
  }

  public static void applyTo(MuteRequest req, PlayerMuteData target) {
    target.setReason(req.reason());
    target.setExpires(req.expires());
    target.setSoft(req.soft());
    target.setSilent(req.silent());
    target.setOnlineOnly(req.onlineOnly());
  }

  public static void applyTo(WarnRequest req, PlayerWarnData target) {
    target.setReason(req.reason());
    target.setPoints(req.points());
    target.setRead(req.read());
    target.setExpires(req.expires());
    // PlayerWarnData has no silent column; silent is a transient broadcast
    // flag tracked separately by storage and surfaced via PlayerWarnedEvent.
  }

  public static void applyTo(NoteRequest req, PlayerNoteData target) {
    target.setMessage(req.message());
  }

  public static void applyTo(ReportRequest req, PlayerReportData target) {
    target.setReason(req.reason());
  }

  public static void applyTo(IpBanRequest req, IpBanData target) {
    target.setReason(req.reason());
    target.setExpires(req.expires());
    target.setSilent(req.silent());
  }

  public static void applyTo(IpMuteRequest req, IpMuteData target) {
    target.setReason(req.reason());
    target.setExpires(req.expires());
    target.setSoft(req.soft());
    target.setSilent(req.silent());
  }

  public static void applyTo(IpRangeBanRequest req, IpRangeBanData target) {
    target.setReason(req.reason());
    target.setExpires(req.expires());
    target.setSilent(req.silent());
  }

  public static void applyTo(NameBanRequest req, NameBanData target) {
    target.setName(req.name());
    target.setReason(req.reason());
    target.setExpires(req.expires());
    target.setSilent(req.silent());
  }

  private static String emptyIfNull(String s) {
    return s == null ? "" : s;
  }
}
