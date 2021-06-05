package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.bukkit.api.events.IpMutedEvent;
import me.confuser.banmanager.bukkit.api.events.PlayerMutedEvent;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerEditBookEvent;

import java.util.UUID;

public class MuteListener implements Listener {
  private final BanManagerPlugin plugin;
  private final CommonMuteListener listener;

  public MuteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonMuteListener(plugin);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnMute(PlayerMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void notifyOnMute(IpMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @EventHandler
  public void blockOnPlayerMute(SignChangeEvent event) {
    if (plugin.getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId()) && event.getPlayer().hasPermission("bm.block.muted.sign")) {
      event.getBlock().breakNaturally();
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void blockOnIpMute(SignChangeEvent event) {
    if (plugin.getIpMuteStorage().isMuted(event.getPlayer().getAddress().getAddress()) && event.getPlayer().hasPermission("bm.block.ipmuted.sign")) {
      event.getBlock().breakNaturally();
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void blockOnPlayerMute(PlayerEditBookEvent event) {
    if (plugin.getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId()) && event.getPlayer().hasPermission("bm.block.muted.book")) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void blockOnIpMute(PlayerEditBookEvent event) {
    if (plugin.getIpMuteStorage().isMuted(event.getPlayer().getAddress().getAddress()) && event.getPlayer().hasPermission("bm.block.ipmuted.book")) {
      event.setCancelled(true);
    }
  }
}
