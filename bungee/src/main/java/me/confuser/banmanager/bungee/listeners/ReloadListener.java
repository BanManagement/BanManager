package me.confuser.banmanager.bungee.listeners;

import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.bungee.BMBungeePlugin;
import me.confuser.banmanager.bungee.api.events.PluginReloadedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

@RequiredArgsConstructor
public class ReloadListener implements Listener {
  private final BMBungeePlugin bungeePlugin;

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onReload(final PluginReloadedEvent event) {
    bungeePlugin.registerChatListener();
  }
}
