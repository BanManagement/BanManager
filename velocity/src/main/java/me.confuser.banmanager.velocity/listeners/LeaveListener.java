package me.confuser.banmanager.velocity.listeners;


import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.listeners.CommonLeaveListener;
import me.confuser.banmanager.velocity.Listener;

public class LeaveListener extends Listener {
  private final CommonLeaveListener listener;

  public LeaveListener(BanManagerPlugin plugin) {
    this.listener = new CommonLeaveListener(plugin);
  }

  @Subscribe
  public void onLeave(DisconnectEvent event) {
    if (ResultedEvent.GenericResult.allowed().isAllowed()) {
      Player player = event.getPlayer();
      listener.onLeave(player.getUniqueId(), player.getUsername());
    }
  }
}
