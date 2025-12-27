package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonBanListener;
import me.confuser.banmanager.sponge.api.events.IpBannedEvent;
import me.confuser.banmanager.sponge.api.events.IpRangeBannedEvent;
import me.confuser.banmanager.sponge.api.events.NameBannedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerBannedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

public class BanListener {
    private final CommonBanListener listener;

    public BanListener(BanManagerPlugin plugin) {
        this.listener = new CommonBanListener(plugin);
    }

    @Listener(order = Order.POST)
    public void notifyOnBan(PlayerBannedEvent event) {
        listener.notifyOnBan(event.getBan(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnIpBan(IpBannedEvent event) {
        listener.notifyOnBan(event.getBan(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnIpRangeBan(IpRangeBannedEvent event) {
        listener.notifyOnBan(event.getBan(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnNameBan(NameBannedEvent event) {
        listener.notifyOnBan(event.getBan(), event.isSilent());
    }
}
