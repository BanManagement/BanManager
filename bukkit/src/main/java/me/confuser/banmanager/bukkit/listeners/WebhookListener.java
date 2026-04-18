package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.bukkit.api.events.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonWebhookListener;
import me.confuser.banmanager.common.data.Webhook;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class WebhookListener implements Listener {
  private CommonWebhookListener listener;

  public WebhookListener(BanManagerPlugin plugin) {
    this.listener = new CommonWebhookListener(plugin);
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnBan(PlayerBannedEvent event) {
    List<Webhook> webhooks = listener.notifyOnBan(event.getBan());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnMute(PlayerMutedEvent event) {
    List<Webhook> webhooks = listener.notifyOnMute(event.getMute());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnBan(IpBannedEvent event) {
    List<Webhook> webhooks = listener.notifyOnBan(event.getBan());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnKick(PlayerKickedEvent event) {
    List<Webhook> webhooks = listener.notifyOnKick(event.getKick());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnWarn(PlayerWarnedEvent event) {
    List<Webhook> webhooks = listener.notifyOnWarn(event.getWarning());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnUnban(PlayerUnbanEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnUnban(IpUnbanEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void notifyOnUnmute(PlayerUnmuteEvent event) {
    List<Webhook> webhooks = listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason());
    sendAll(webhooks, event.isSilent());
  }

  @EventHandler(priority = EventPriority.MONITOR)
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
