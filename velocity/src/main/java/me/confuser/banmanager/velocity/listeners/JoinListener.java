package me.confuser.banmanager.velocity.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.velocity.BMVelocityPlugin;
import me.confuser.banmanager.velocity.VelocityPlayer;
import me.confuser.banmanager.velocity.VelocityServer;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.velocity.Listener;


public class JoinListener extends Listener {
  private final CommonJoinListener listener;
  private BMVelocityPlugin plugin;

  public JoinListener(BMVelocityPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin.getPlugin());
  }

  @Subscribe(order = PostOrder.EARLY)
  public void banCheck(ServerPreConnectEvent event) {

    plugin.getPlugin().getScheduler().runAsync(() -> {
      Player player = event.getPlayer();
      listener.banCheck(player.getUniqueId(), player.getUsername(), IPUtils.toIPAddress(player.getRemoteAddress().getAddress()), new BanJoinHandler(event));

      if (!event.getResult().isAllowed()) {
        listener.onPreJoin(player.getUniqueId(), player.getUsername(), IPUtils.toIPAddress(player.getRemoteAddress().getAddress()));
      }
    });
  }

  @Subscribe(order = PostOrder.NORMAL)
  public void onJoin(ServerPostConnectEvent event) {
    listener.onJoin(new VelocityPlayer(java.util.Optional.ofNullable(event.getPlayer()), plugin.getPlugin().getConfig().isOnlineMode()));
  }

  @Subscribe(order = PostOrder.FIRST)
  public void onPlayerLogin(ServerPreConnectEvent event) {
    listener.onPlayerLogin(new VelocityPlayer(java.util.Optional.ofNullable(event.getPlayer()), plugin.getPlugin().getConfig().isOnlineMode()), new LoginHandler(event));
  }

  @RequiredArgsConstructor
  private class BanJoinHandler implements CommonJoinHandler {
    private final ServerPreConnectEvent event;

    @Override
    public void handleDeny(Message message) {
      // @TODO Reimplement denial messsage... CHECK DA DOCS!
      // VelocityServer.formatMessage(message.toString())
      event.setResult(ServerPreConnectEvent.ServerResult.denied());
    }
  }

  @RequiredArgsConstructor
  private class LoginHandler implements CommonJoinHandler {
    private final ServerPreConnectEvent event;

    @Override
    public void handleDeny(Message message) {
      event.getPlayer().disconnect(VelocityServer.formatMessage(message.toString()));
    }
  }
}
