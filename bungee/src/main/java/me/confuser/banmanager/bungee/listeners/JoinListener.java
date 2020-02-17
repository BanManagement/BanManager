package me.confuser.banmanager.bungee.listeners;

import me.confuser.banmanager.bungee.BungeePlayer;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class JoinListener implements Listener {
  private final CommonJoinListener listener;
  private BanManagerPlugin plugin;

  public JoinListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin);
  }

  @EventHandler
  public void onJoin(PostLoginEvent event) {
    listener.onJoin(new BungeePlayer(event.getPlayer(), plugin.getConfig().isOnlineMode()));
  }
}
