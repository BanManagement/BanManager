package me.confuser.banmanager.bukkit.listeners;

import me.confuser.bukkitutil.Message;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class BukkitUpdateListener implements Listener {

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!event.getPlayer().hasPermission("bm.notify.update")) return;

    Message.get("update.notify").sendTo(event.getPlayer());
    event.getPlayer().sendMessage(ChatColor.GOLD + "http://dev.bukkit.org/bukkit-plugins/ban-management/");
  }
}
