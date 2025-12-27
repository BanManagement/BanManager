package me.confuser.banmanager.sponge.listeners;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.sponge.SpongePlayer;
import me.confuser.banmanager.sponge.SpongeServer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ServerSideConnectionEvent;

import java.net.InetAddress;

public class JoinListener {
    private final CommonJoinListener listener;
    private final BanManagerPlugin plugin;

    public JoinListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new CommonJoinListener(plugin);
    }

    @Listener(order = Order.LAST)
    public void onAuth(ServerSideConnectionEvent.Auth event) {
        InetAddress address = event.connection().address().getAddress();
        String name = event.profile().name().orElse("");

        try {
            listener.banCheck(
                event.profile().uniqueId(),
                name,
                IPUtils.toIPAddress(address),
                new BanJoinHandler(plugin, event)
            );
        } catch (Exception e) {
            plugin.getLogger().warning("Error during banCheck: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Listener(order = Order.LAST)
    public void onLogin(ServerSideConnectionEvent.Login event) {
        if (event.isCancelled()) return;

        InetAddress address = event.connection().address().getAddress();
        String name = event.profile().name().orElse("");

        listener.onPreJoin(
            event.profile().uniqueId(),
            name,
            IPUtils.toIPAddress(address)
        );
    }

    @Listener(order = Order.LAST)
    public void onJoin(ServerSideConnectionEvent.Join event) {
        ServerPlayer player = event.player();

        listener.onJoin(new SpongePlayer(player, plugin.getConfig().isOnlineMode()));

        listener.onPlayerLogin(
            new SpongePlayer(player, plugin.getConfig().isOnlineMode(), player.connection().address().getAddress()),
            new LoginHandler(player)
        );
    }

    @RequiredArgsConstructor
    private class BanJoinHandler implements CommonJoinHandler {
        private final BanManagerPlugin plugin;
        private final ServerSideConnectionEvent.Auth event;
        @Getter
        private boolean isDenied = false;

        @Override
        public void handlePlayerDeny(PlayerData player, Message message) {
            plugin.getServer().callEvent("PlayerDeniedEvent", player, message);
            handleDeny(message);
        }

        @Override
        public void handleDeny(Message message) {
            isDenied = true;
            event.setCancelled(true);
            event.setMessage(SpongeServer.formatMessage(message.toString()));
        }
    }

    @RequiredArgsConstructor
    private class LoginHandler implements CommonJoinHandler {
        private final ServerPlayer player;

        @Override
        public void handlePlayerDeny(PlayerData player, Message message) {
            handleDeny(message);
        }

        @Override
        public void handleDeny(Message message) {
            player.kick(SpongeServer.formatMessage(message.toString()));
        }
    }
}
