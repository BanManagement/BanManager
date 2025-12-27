package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonDiscordListener;
import me.confuser.banmanager.sponge.api.events.*;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

public class DiscordListener {
    private final CommonDiscordListener listener;

    public DiscordListener(BanManagerPlugin plugin) {
        this.listener = new CommonDiscordListener(plugin);
    }

    @Listener(order = Order.POST)
    public void notifyOnBan(PlayerBannedEvent event) {
        Object[] data = listener.notifyOnBan(event.getBan());

        if (event.isSilent() && (boolean) data[2]) return;

        send(data);
    }

    @Listener(order = Order.POST)
    public void notifyOnMute(PlayerMutedEvent event) {
        Object[] data = listener.notifyOnMute(event.getMute());

        if (event.isSilent() && (boolean) data[2]) return;

        send(data);
    }

    @Listener(order = Order.POST)
    public void notifyOnWarn(PlayerWarnedEvent event) {
        Object[] data = listener.notifyOnWarn(event.getWarning());

        if (event.isSilent() && (boolean) data[2]) return;

        send(data);
    }

    @Listener(order = Order.POST)
    public void notifyOnBan(IpBannedEvent event) {
        Object[] data = listener.notifyOnBan(event.getBan());

        if (event.isSilent() && (boolean) data[2]) return;

        send(data);
    }

    @Listener(order = Order.POST)
    public void notifyOnKick(PlayerKickedEvent event) {
        Object[] data = listener.notifyOnKick(event.getKick());

        if (event.isSilent() && (boolean) data[2]) return;

        send(data);
    }

    @Listener(order = Order.POST)
    public void notifyOnUnban(PlayerUnbanEvent event) {
        send(listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason()));
    }

    @Listener(order = Order.POST)
    public void notifyOnUnban(IpUnbanEvent event) {
        send(listener.notifyOnUnban(event.getBan(), event.getActor(), event.getReason()));
    }

    @Listener(order = Order.POST)
    public void notifyOnUnmute(PlayerUnmuteEvent event) {
        send(listener.notifyOnUnmute(event.getMute(), event.getActor(), event.getReason()));
    }

    @Listener(order = Order.POST)
    public void notifyOnReport(PlayerReportedEvent event) {
        Object[] data = listener.notifyOnReport(event.getReport(), event.getReport().getActor(), event.getReport().getReason());

        if (event.isSilent() && (boolean) data[2]) return;

        send(data);
    }

    private void send(Object[] data) {
        String url = (String) data[0];
        String payload = (String) data[1];

        if (url == null || payload == null || url.isEmpty() || payload.isEmpty()) return;

        listener.send(url, payload);
    }
}
