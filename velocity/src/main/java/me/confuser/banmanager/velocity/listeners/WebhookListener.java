package me.confuser.banmanager.velocity.listeners;

import me.confuser.banmanager.velocity.api.events.*;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonWebhookListener;
import me.confuser.banmanager.common.data.Webhook;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.PostOrder;

import java.util.List;

public class WebhookListener extends Listener {
  private CommonWebhookListener listener;

  public WebhookListener(BanManagerPlugin plugin) {
    this.listener = new CommonWebhookListener(plugin);
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnBan(PlayerBannedEvent event) {
    List<Webhook> webhooks = listener.notifyOnBan(event.getBan());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnMute(PlayerMutedEvent event) {
    List<Webhook> webhooks = listener.notifyOnMute(event.getMute());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnBan(IpBannedEvent event) {
    List<Webhook> webhooks = listener.notifyOnBan(event.getBan());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnKick(PlayerKickedEvent event) {
    List<Webhook> webhooks = listener.notifyOnKick(event.getKick());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    List<Webhook> webhooks = listener.notifyOnWarn(event.getWarning());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnUnban(PlayerUnbanEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnUnban(IpUnbanEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnUnmute(PlayerUnmuteEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @Subscribe(order = PostOrder.LAST)
  public void notifyOnReport(PlayerReportedEvent event) {
    List<Webhook> webhooks = listener.notifyOnReport(event.getReport(), event.getReport().getActor(), event.getReport().getReason());
    sendAll(webhooks, event.isSilent());
  }

  private void sendAll(List<Webhook> webhooks, boolean isSilent) {
    for (Webhook data : webhooks) {
      if (isSilent && data.ignoreSilent()) continue;
      if (data.url() == null || data.payload() == null || data.url().isEmpty() || data.payload().isEmpty()) continue;
      listener.sendAsync(data);
    }
  }
}
