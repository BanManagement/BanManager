package me.confuser.banmanager.bukkit.listeners;

import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.bukkit.BMBukkitPlugin;
import me.confuser.banmanager.bukkit.api.events.PluginReloadedEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

@RequiredArgsConstructor
public class ReloadListener implements Listener {
  private final BMBukkitPlugin bukkitPlugin;

  @EventHandler(priority = EventPriority.MONITOR)
  public void onReload(final PluginReloadedEvent event) {
    bukkitPlugin.registerChatListener();
  }
}
