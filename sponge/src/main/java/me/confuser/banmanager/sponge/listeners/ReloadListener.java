package me.confuser.banmanager.sponge.listeners;

import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.sponge.BMSpongePlugin;
import me.confuser.banmanager.sponge.api.events.PluginReloadedEvent;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;

@RequiredArgsConstructor
public class ReloadListener {
  private final BMSpongePlugin spongePlugin;

  @Listener(order = Order.POST)
  public void onReload(final PluginReloadedEvent event) {
    spongePlugin.registerChatListener();
  }
}
