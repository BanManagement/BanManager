package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonLeaveListener;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

public class LeaveListener {
    private final CommonLeaveListener listener;
    private final BanManagerPlugin plugin;

    public LeaveListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new CommonLeaveListener(plugin);
    }

    @Listener(order = Order.LAST)
    public void onDisconnect(ServerSideConnectionEvent.Disconnect event, @First ServerPlayer player) {
        listener.onLeave(player.uniqueId(), player.name());
    }
}
