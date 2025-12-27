package me.confuser.banmanager.sponge.listeners;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonChatHandler;
import me.confuser.banmanager.common.listeners.CommonChatListener;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.message.PlayerChatEvent;

public class ChatListener {
    private final CommonChatListener listener;
    private final BanManagerPlugin plugin;

    public ChatListener(BanManagerPlugin plugin) {
        this.plugin = plugin;
        this.listener = new CommonChatListener(plugin);
    }

    /**
     * Listen to PlayerChatEvent.Submit which is the cancellable sub-event.
     * This fires when the player submits a chat message, before it's broadcast.
     */
    @Listener(order = Order.EARLY)
    public void onPlayerChat(PlayerChatEvent.Submit event, @First ServerPlayer player) {
        CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.uniqueId());
        if (commonPlayer == null) return;

        String message = PlainTextComponentSerializer.plainText().serialize(event.message());
        ChatHandler handler = new ChatHandler(event, player);

        boolean playerChatCancelled = listener.onPlayerChat(commonPlayer, handler, message);
        boolean ipChatCancelled = listener.onIpChat(commonPlayer, commonPlayer.getAddress(), handler, message);

        // Cancel the event if the player is muted (and not soft-muted)
        // Soft mute is handled by the handler which shows message only to the player
        if ((playerChatCancelled || ipChatCancelled) && !handler.isSoftMuted()) {
            event.setCancelled(true);
        }
    }

    @RequiredArgsConstructor
    private class ChatHandler implements CommonChatHandler {
        private final PlayerChatEvent.Submit event;
        private final ServerPlayer player;
        @Getter
        private boolean isSoftMuted = false;

        @Override
        public void handleSoftMute() {
            isSoftMuted = true;
            // For soft mute, send the message only to the player and cancel the broadcast
            player.sendMessage(event.message());
            event.setCancelled(true);
        }
    }
}
