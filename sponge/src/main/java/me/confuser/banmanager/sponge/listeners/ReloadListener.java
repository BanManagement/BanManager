package me.confuser.banmanager.sponge.listeners;

import me.confuser.banmanager.sponge.BMSpongePlugin;
import me.confuser.banmanager.sponge.api.events.PluginReloadedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

public class ReloadListener {
    private final BMSpongePlugin spongePlugin;

    public ReloadListener(BMSpongePlugin spongePlugin) {
        this.spongePlugin = spongePlugin;
    }

    @Listener(order = Order.POST)
    public void onReload(PluginReloadedEvent event) {
        spongePlugin.registerChatListener();
    }
}
