package me.confuser.banmanager.fabric.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonDiscordListener;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.common.data.*;

public class DiscordListener {

  private final CommonDiscordListener listener;

  public DiscordListener(BanManagerPlugin plugin) {
    this.listener = new CommonDiscordListener(plugin);

    BanManagerEvents.PLAYER_BANNED_EVENT.register(this::notifyOnBan);
    BanManagerEvents.PLAYER_MUTED_EVENT.register(this::notifyOnMute);
    BanManagerEvents.IP_BANNED_EVENT.register(this::notifyOnBan);
    BanManagerEvents.PLAYER_KICKED_EVENT.register(this::notifyOnKick);
    BanManagerEvents.PLAYER_WARNED_EVENT.register(this::notifyOnWarn);
    BanManagerEvents.PLAYER_UNBAN_EVENT.register(this::notifyOnUnban);
    BanManagerEvents.IP_UNBAN_EVENT.register(this::notifyOnUnban);
    BanManagerEvents.PLAYER_UNMUTE_EVENT.register(this::notifyOnUnmute);
    BanManagerEvents.PLAYER_REPORTED_EVENT.register(this::notifyOnReport);
  }

  private void notifyOnBan(PlayerBanData banData, boolean silent) {
    Object[] data = listener.notifyOnBan(banData);

    if (silent && (boolean) data[2]) return;

    send(data);
  }

  private void notifyOnMute(PlayerMuteData muteData, boolean silent) {
    Object[] data = listener.notifyOnMute(muteData);

    if (silent && (boolean) data[2]) return;

    send(data);
  }

  private void notifyOnBan(IpBanData banData, boolean silent) {
    Object[] data = listener.notifyOnBan(banData);

    if (silent && (boolean) data[2]) return;

    send(data);
  }

  private void notifyOnKick(PlayerKickData kickData, boolean silent) {
    Object[] data = listener.notifyOnKick(kickData);

    if (silent && (boolean) data[2]) return;

    send(data);
  }

  private void notifyOnWarn(PlayerWarnData warnData, boolean silent) {
    Object[] data = listener.notifyOnWarn(warnData);

    if (silent && (boolean) data[2]) return;

    send(data);
  }

  private void notifyOnUnban(PlayerBanData banData, PlayerData actor, String reason) {
    Object[] data = listener.notifyOnUnban(banData, actor, reason);

    send(data);
  }

  private void notifyOnUnban(IpBanData banData, PlayerData actor, String reason) {
    Object[] data = listener.notifyOnUnban(banData, actor, reason);

    send(data);
  }

  private void notifyOnUnmute(PlayerMuteData muteData, PlayerData actor, String reason) {
    Object[] data = listener.notifyOnUnmute(muteData, actor, reason);

    send(data);
  }

  private void notifyOnReport(PlayerReportData reportData, boolean silent) {
    Object[] data = listener.notifyOnReport(reportData, reportData.getActor(), reportData.getReason());

    if (silent && (boolean) data[2]) return;

    send(data);
  }

  private void send(Object[] data) {
    String url = (String) data[0];
    String payload = (String) data[1];

    if (url == null || payload == null || url.isEmpty() || payload.isEmpty()) return;

    listener.send(url, payload);
  }
}
