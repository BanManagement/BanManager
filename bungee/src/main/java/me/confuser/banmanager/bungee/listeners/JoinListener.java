package me.confuser.banmanager.bungee.listeners;

import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.bungee.BMBungeePlugin;
import me.confuser.banmanager.bungee.BungeePlayer;
import me.confuser.banmanager.bungee.BungeeServer;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class JoinListener implements Listener {
  private final CommonJoinListener listener;
  private BMBungeePlugin plugin;

  public JoinListener(BMBungeePlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin.getPlugin());
  }

  @EventHandler(priority = EventPriority.LOW)
  public void banCheck(LoginEvent event) {
    event.registerIntent(plugin);

    plugin.getPlugin().getScheduler().runAsync(() -> {
      listener.banCheck(event.getConnection().getUniqueId(), event.getConnection().getName(), IPUtils.toIPAddress(event.getConnection().getAddress().getAddress()), new BanJoinHandler(event));

      if (!event.isCancelled()) {
        listener.onPreJoin(event.getConnection().getUniqueId(), event.getConnection().getName(), IPUtils.toIPAddress(event.getConnection().getAddress().getAddress()));
      }

      event.completeIntent(plugin);
    });
  }

  @EventHandler
  public void onJoin(PostLoginEvent event) {
    listener.onJoin(new BungeePlayer(event.getPlayer(), plugin.getPlugin().getConfig().isOnlineMode()));
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerLogin(PostLoginEvent event) {
    listener.onPlayerLogin(new BungeePlayer(event.getPlayer(), plugin.getPlugin().getConfig().isOnlineMode()), new LoginHandler(event));
  }

  @RequiredArgsConstructor
  private class BanJoinHandler implements CommonJoinHandler {
    private final LoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.setCancelled(true);
      event.setCancelReason(BungeeServer.formatMessage(message.toString()));
    }
  }

  @RequiredArgsConstructor
  private class LoginHandler implements CommonJoinHandler {
    private final PostLoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.getPlayer().disconnect(BungeeServer.formatMessage(message.toString()));
    }
  }
}
