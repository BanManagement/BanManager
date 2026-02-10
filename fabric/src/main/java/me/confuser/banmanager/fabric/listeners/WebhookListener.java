package me.confuser.banmanager.fabric.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonWebhookListener;
import me.confuser.banmanager.common.listeners.CommonWebhookListener.WebhookData;
import me.confuser.banmanager.fabric.BanManagerEvents;
import me.confuser.banmanager.common.data.*;

import java.util.List;

public class WebhookListener {

  private final CommonWebhookListener listener;

  public WebhookListener(BanManagerPlugin plugin) {
    this.listener = new CommonWebhookListener(plugin);

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
    List<WebhookData> webhooks = listener.notifyOnBan(banData);
    sendAll(webhooks, silent);
  }

  private void notifyOnMute(PlayerMuteData muteData, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnMute(muteData);
    sendAll(webhooks, silent);
  }

  private void notifyOnBan(IpBanData banData, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnBan(banData);
    sendAll(webhooks, silent);
  }

  private void notifyOnKick(PlayerKickData kickData, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnKick(kickData);
    sendAll(webhooks, silent);
  }

  private void notifyOnWarn(PlayerWarnData warnData, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnWarn(warnData);
    sendAll(webhooks, silent);
  }

  private void notifyOnUnban(PlayerBanData banData, PlayerData actor, String reason, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnUnban(banData, actor, reason);
    sendAll(webhooks, silent);
  }

  private void notifyOnUnban(IpBanData banData, PlayerData actor, String reason, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnUnban(banData, actor, reason);
    sendAll(webhooks, silent);
  }

  private void notifyOnUnmute(PlayerMuteData muteData, PlayerData actor, String reason, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnUnmute(muteData, actor, reason);
    sendAll(webhooks, silent);
  }

  private void notifyOnReport(PlayerReportData reportData, boolean silent) {
    List<WebhookData> webhooks = listener.notifyOnReport(reportData, reportData.getActor(), reportData.getReason());
    sendAll(webhooks, silent);
  }

  private void sendAll(List<WebhookData> webhooks, boolean isSilent) {
    for (WebhookData data : webhooks) {
      if (isSilent && !data.ignoreSilent) continue;
      if (data.url == null || data.payload == null || data.url.isEmpty() || data.payload.isEmpty()) continue;
      listener.sendAsync(data);
    }
  }
}
