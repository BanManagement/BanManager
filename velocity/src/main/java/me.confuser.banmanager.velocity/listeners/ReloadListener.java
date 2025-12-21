package me.confuser.banmanager.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.velocity.BMVelocityPlugin;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.api.events.PluginReloadedEvent;

@RequiredArgsConstructor
public class ReloadListener extends Listener {
  private final BMVelocityPlugin velocityPlugin;

  @Subscribe(order = PostOrder.LAST)
  public void onReload(final PluginReloadedEvent event) {
    velocityPlugin.registerChatListener();
  }
}
