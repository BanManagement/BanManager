package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.listeners.CommonCommandListener;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.First;

public class CommandListener {

  private final CommonCommandListener listener;
  private BanManagerPlugin plugin;

  public CommandListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonCommandListener(plugin);
  }

  @Listener(order = Order.FIRST, beforeModifications = true)
  public void onCommand(SendCommandEvent event, @First Player player) {
    CommonPlayer commonPlayer = plugin.getServer().getPlayer(player.getUniqueId());
    // Split the command
    String[] args = event.getArguments().split(" ", 6);
    String cmd = event.getCommand().toLowerCase();

    if (listener.onCommand(commonPlayer, cmd, args)) {
      event.setCancelled(true);
    }
  }
}
