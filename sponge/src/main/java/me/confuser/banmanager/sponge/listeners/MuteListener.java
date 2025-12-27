package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;
import me.confuser.banmanager.sponge.api.events.IpMutedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerMutedEvent;
import org.spongepowered.api.block.entity.Sign;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class MuteListener {
    private final BanManagerPlugin plugin;
    private final CommonMuteListener listener;

    public MuteListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new CommonMuteListener(plugin);
    }

    @Listener(order = Order.POST)
    public void notifyOnMute(PlayerMutedEvent event) {
        listener.notifyOnMute(event.getMute(), event.isSilent());
    }

    @Listener(order = Order.POST)
    public void notifyOnMute(IpMutedEvent event) {
        listener.notifyOnMute(event.getMute(), event.isSilent());
    }

    @Listener(order = Order.DEFAULT)
    public void blockOnPlayerMute(ChangeSignEvent event, @Root ServerPlayer player) {
        if (plugin.getPlayerMuteStorage().isMuted(player.uniqueId()) && player.hasPermission("bm.block.muted.sign")) {
            event.setCancelled(true);
        }
    }

    @Listener(order = Order.DEFAULT)
    public void blockOnIpMute(ChangeSignEvent event, @Root ServerPlayer player) {
        if (plugin.getIpMuteStorage().isMuted(player.connection().address().getAddress()) && player.hasPermission("bm.block.ipmuted.sign")) {
            event.setCancelled(true);
        }
    }
}
