package me.confuser.banmanager.bukkit.listeners;

import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.bukkit.BukkitPlayer;
import me.confuser.banmanager.bukkit.BukkitServer;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;

public class JoinListener implements Listener {
  private final CommonJoinListener listener;
  private BanManagerPlugin plugin;

  public JoinListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void banCheck(final AsyncPlayerPreLoginEvent event) {
    listener.banCheck(event.getUniqueId(), event.getName(), IPUtils.toIPAddress(event.getAddress()), new BanJoinHandler(event));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(AsyncPlayerPreLoginEvent event) {
    if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
      return;
    }

    listener.onPreJoin(event.getUniqueId(), event.getName(), IPUtils.toIPAddress(event.getAddress()));
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onJoin(final PlayerJoinEvent event) {
    listener.onJoin(new BukkitPlayer(event.getPlayer(), plugin.getConfig().isOnlineMode()));
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerLogin(final PlayerLoginEvent event) {
    if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
      return;
    }

    listener.onPlayerLogin(new BukkitPlayer(event.getPlayer(), plugin.getConfig().isOnlineMode(), event.getAddress()), new LoginHandler(event));
  }

  @RequiredArgsConstructor
  private class BanJoinHandler implements CommonJoinHandler {
    private final AsyncPlayerPreLoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_BANNED);
      event.setKickMessage(BukkitServer.formatMessage(message.toString()));
    }
  }

  @RequiredArgsConstructor
  private class LoginHandler implements CommonJoinHandler {
    private final PlayerLoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message.toString());
    }
  }
}