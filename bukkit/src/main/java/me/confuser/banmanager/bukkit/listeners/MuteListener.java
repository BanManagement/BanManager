package me.confuser.banmanager.bukkit.listeners;

import me.confuser.banmanager.common.BanManagerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerEditBookEvent;

/**
 * Handles Bukkit-specific events that should be blocked while a player is
 * muted (sign changes, book edits). Notification dispatch lives in the
 * cross-platform {@link me.confuser.banmanager.common.listeners.CommonMuteListener}.
 */
public class MuteListener implements Listener {
  private final BanManagerPlugin plugin;

  public MuteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
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
