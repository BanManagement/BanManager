package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonWebhookListener;
import me.confuser.banmanager.common.listeners.CommonWebhookListener.WebhookData;
import me.confuser.banmanager.sponge.api.events.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

import java.util.List;

public class WebhookListener {
    private final CommonWebhookListener listener;

    public WebhookListener(BanManagerPlugin plugin) {
        this.listener = new CommonWebhookListener(plugin);
    }

    @Listener(order = Order.POST)
    public void notifyOnBan(PlayerBannedEvent event) {
        List<WebhookData> webhooks = listener.notifyOnBan(event.getBan());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnMute(PlayerMutedEvent event) {
        List<WebhookData> webhooks = listener.notifyOnMute(event.getMute());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnWarn(PlayerWarnedEvent event) {
        List<WebhookData> webhooks = listener.notifyOnWarn(event.getWarning());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnBan(IpBannedEvent event) {
        List<WebhookData> webhooks = listener.notifyOnBan(event.getBan());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnKick(PlayerKickedEvent event) {
        List<WebhookData> webhooks = listener.notifyOnKick(event.getKick());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnUnban(PlayerUnbanEvent event) {
        List<WebhookData> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnUnban(IpUnbanEvent event) {
        List<WebhookData> webhooks = listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnUnmute(PlayerUnmuteEvent event) {
        List<WebhookData> webhooks = listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason());
        sendAll(webhooks, event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnReport(PlayerReportedEvent event) {
        List<WebhookData> webhooks = listener.notifyOnReport(event.getReport(), event.getReport().getActor(), event.getReport().getReason());
        sendAll(webhooks, event.isSilent());
    }

    private void sendAll(List<WebhookData> webhooks, boolean isSilent) {
        for (WebhookData data : webhooks) {
            if (isSilent && data.ignoreSilent) continue;
            if (data.url == null || data.payload == null || data.url.isEmpty() || data.payload.isEmpty()) continue;
            listener.sendAsync(data);
        }
    }
}
