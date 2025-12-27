package me.confuser.banmanager.sponge.listeners;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonMuteListener;
import me.confuser.banmanager.sponge.api.events.IpMutedEvent;
import me.confuser.banmanager.sponge.api.events.PlayerMutedEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.filter.IsCancelled;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.util.Tristate;

public class MuteListener {
  private final BanManagerPlugin plugin;
  private final CommonMuteListener listener;

  public MuteListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonMuteListener(plugin);
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnMute(PlayerMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @IsCancelled(Tristate.UNDEFINED)
  @Listener(order = Order.POST)
  public void notifyOnMute(IpMutedEvent event) {
    listener.notifyOnMute(event.getMute(), event.isSilent());
  }

  @Listener(order = Order.DEFAULT)
  public void blockOnPlayerMute(ChangeSignEvent event, @Root Player player) {
    if (plugin.getPlayerMuteStorage().isMuted(player.getUniqueId()) && player.hasPermission("bm.block.muted.sign")) {
      event.getTargetTile().getLocation().removeBlock();
      event.setCancelled(true);
    }
  }

  @Listener(order = Order.DEFAULT)
  public void blockOnIpMute(ChangeSignEvent event, @Root Player player) {
    if (plugin.getIpMuteStorage().isMuted(player.getConnection().getAddress().getAddress()) && player.hasPermission("bm.block.ipmuted.sign")) {
      event.getTargetTile().getLocation().removeBlock();
      event.setCancelled(true);
    }
  }

  // Book events unsupported by Sponge API
//  @Listener(order = Order.DEFAULT)
//  public void blockOnPlayerMute(PlayerEditBookEvent event, @Root Player player) {
//    if (plugin.getPlayerMuteStorage().isMuted(player.getUniqueId()) && player.hasPermission("bm.block.muted.book")) {
//      event.setCancelled(true);
//    }
//  }
//
//  @Listener(order = Order.DEFAULT)
//  public void blockOnIpMute(PlayerEditBookEvent event, @Root Player player) {
//    if (plugin.getIpMuteStorage().isMuted(player.getConnection().getAddress().getAddress()) && player.hasPermission("bm.block.ipmuted.book")) {
//      event.setCancelled(true);
//    }
//  }
}
