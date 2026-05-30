package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.entity.ChangeSignEvent;
import org.spongepowered.api.event.filter.cause.Root;

public class MuteListener {
    private final BanManagerPlugin plugin;

    public MuteListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
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
