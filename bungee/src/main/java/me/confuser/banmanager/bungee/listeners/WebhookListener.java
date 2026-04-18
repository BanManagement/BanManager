package me.confuser.banmanager.bungee.listeners;

import me.confuser.banmanager.bungee.api.events.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonWebhookListener;
import me.confuser.banmanager.common.data.Webhook;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;

public class WebhookListener implements Listener {
  private CommonWebhookListener listener;

  public WebhookListener(BanManagerPlugin plugin) {
    this.listener = new CommonWebhookListener(plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnBan(PlayerBannedEvent event) {
    List<Webhook> webhooks = listener.notifyOnBan(event.getBan());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnMute(PlayerMutedEvent event) {
    List<Webhook> webhooks = listener.notifyOnMute(event.getMute());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnBan(IpBannedEvent event) {
    List<Webhook> webhooks = listener.notifyOnBan(event.getBan());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnKick(PlayerKickedEvent event) {
    List<Webhook> webhooks = listener.notifyOnKick(event.getKick());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    List<Webhook> webhooks = listener.notifyOnWarn(event.getWarning());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnUnban(PlayerUnbanEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnUnban(IpUnbanEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void notifyOnUnmute(PlayerUnmuteEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
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
