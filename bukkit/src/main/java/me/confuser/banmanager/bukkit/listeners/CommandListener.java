package me.confuser.banmanager.bukkit.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import me.confuser.banmanager.common.util.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

  private final CommonCommandListener listener;
  private BanManagerPlugin plugin;

  public CommandListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonCommandListener(plugin);
  }

  @EventHandler
  public void onCommand(PlayerCommandPreprocessEvent event) {
    CommonPlayer player = plugin.getServer().getPlayer(event.getPlayer().getUniqueId());
    // Split the command
    String[] args = event.getMessage().split(" ", 6);
    // Get rid of the first /
    String cmd = args[0].replace("/", "").toLowerCase();

    if (listener.onCommand(player, cmd, args)) {
      event.setCancelled(true);
    }
  }
}
