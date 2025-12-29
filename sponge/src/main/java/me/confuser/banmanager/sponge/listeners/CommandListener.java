package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.ExecuteCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

public class CommandListener {
    private final CommonCommandListener listener;
    private final BanManagerPlugin plugin;

    public CommandListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new CommonCommandListener(plugin);
    }

    @Listener(order = Order.FIRST)
    public void onCommand(ExecuteCommandEvent.Pre event, @First ServerPlayer player) {
        CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.uniqueId());
        if (commonPlayer == null) return;

        String command = event.command().toLowerCase();
        String[] args = event.arguments().split(" ", 6);

        if (listener.onCommand(commonPlayer, command, args)) {
            event.setCancelled(true);
        }
    }
}


