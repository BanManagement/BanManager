package me.confuser.banmanager.fabric.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonHooksListener;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.common.data.*;

public class HookListener {

  private final CommonHooksListener listener;

  public HookListener(BanManagerPlugin plugin) {
    this.listener = new CommonHooksListener(plugin);

    BanManagerEvents.PLAYER_BAN_EVENT.register(this::onBan);
    BanManagerEvents.PLAYER_BANNED_EVENT.register(this::onBan);
    BanManagerEvents.PLAYER_UNBAN_EVENT.register(this::onUnban);
    BanManagerEvents.PLAYER_MUTE_EVENT.register(this::onMute);
    BanManagerEvents.PLAYER_MUTED_EVENT.register(this::onMute);
    BanManagerEvents.PLAYER_UNMUTE_EVENT.register(this::onUnmute);
    BanManagerEvents.IP_BAN_EVENT.register(this::onBan);
    BanManagerEvents.IP_BANNED_EVENT.register(this::onBan);
    BanManagerEvents.IP_UNBAN_EVENT.register(this::onUnban);
    BanManagerEvents.IP_RANGE_BAN_EVENT.register(this::onBan);
    BanManagerEvents.IP_RANGE_BANNED_EVENT.register(this::onBan);
    BanManagerEvents.IP_RANGE_UNBAN_EVENT.register(this::onUnban);
    BanManagerEvents.PLAYER_WARN_EVENT.register(this::onWarn);
    BanManagerEvents.PLAYER_WARNED_EVENT.register(this::onWarn);
    BanManagerEvents.PLAYER_NOTE_CREATED_EVENT.register(this::onNote);
    BanManagerEvents.PLAYER_REPORT_EVENT.register(this::onReport);
    BanManagerEvents.PLAYER_REPORTED_EVENT.register(this::onReport);
  }

  private boolean onBan(PlayerBanData banData, BanManagerEvents.SilentValue silent) {
    listener.onBan(banData, silent.isSilent());
    return true;
  }

  private void onBan(PlayerBanData banData, boolean silent) {
    listener.onBan(banData, silent);
  }

  private void onUnban(PlayerBanData banData, PlayerData actor, String reason, boolean silent) {
    listener.onUnban(banData, actor, reason);
  }

  private boolean onMute(PlayerMuteData muteData, BanManagerEvents.SilentValue silent) {
    listener.onMute(muteData, silent.isSilent());
    return true;
  }

  private void onMute(PlayerMuteData muteData, boolean silent) {
    listener.onMute(muteData, silent);
  }

  private void onUnmute(PlayerMuteData muteData, PlayerData actor, String reason, boolean silent) {
    listener.onUnmute(muteData, actor, reason);
  }

  private boolean onBan(IpBanData banData, BanManagerEvents.SilentValue silent) {
    listener.onBan(banData, silent.isSilent());
    return true;
  }

  private void onBan(IpBanData banData, boolean silent) {
    listener.onBan(banData, silent);
  }

  private void onUnban(IpBanData banData, PlayerData actor, String reason, boolean silent) {
    listener.onUnban(banData, actor, reason);
  }

  private boolean onBan(IpRangeBanData banData, BanManagerEvents.SilentValue silent) {
    listener.onBan(banData, silent.isSilent());
    return true;
  }

  private void onBan(IpRangeBanData banData, boolean silent) {
    listener.onBan(banData, silent);
  }

  private void onUnban(IpRangeBanData banData, PlayerData actor, String reason, boolean silent) {
    listener.onUnban(banData, actor, reason);
  }

  private boolean onWarn(PlayerWarnData warnData, BanManagerEvents.SilentValue silent) {
    listener.onWarn(warnData, silent.isSilent());
    return true;
  }

  private void onWarn(PlayerWarnData warnData, boolean silent) {
    listener.onWarn(warnData, silent);
  }

  private void onNote(PlayerNoteData noteData) {
    listener.onNote(noteData);
  }

  private boolean onReport(PlayerReportData reportData, BanManagerEvents.SilentValue silent) {
    listener.onReport(reportData, silent.isSilent());
    return true;
  }

  private void onReport(PlayerReportData reportData, boolean silent) {
    listener.onReport(reportData, silent);
  }
}
