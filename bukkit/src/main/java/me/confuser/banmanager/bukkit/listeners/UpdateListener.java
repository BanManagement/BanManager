package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateListener implements Listener {

  private BanManagerPlugin plugin;

  public UpdateListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!event.getPlayer().hasPermission("bm.notify.update")) return;

    Message.get("update.notify").sendTo(plugin.getServer().getPlayer(event.getPlayer().getUniqueId()));
    event.getPlayer().sendMessage(ChatColor.GOLD + "http://dev.bukkit.org/bukkit-plugins/ban-management/");
  }
}
