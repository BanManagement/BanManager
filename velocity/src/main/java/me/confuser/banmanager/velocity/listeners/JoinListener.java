package me.confuser.banmanager.velocity.listeners;

import com.velocitypowered.api.event.*;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.velocity.BMVelocityPlugin;
import me.confuser.banmanager.velocity.Listener;
import me.confuser.banmanager.velocity.VelocityPlayer;
import me.confuser.banmanager.velocity.VelocityServer;
import net.kyori.adventure.text.Component;

public class JoinListener extends Listener {
  private final CommonJoinListener listener;
  private BMVelocityPlugin plugin;

  public JoinListener(BMVelocityPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin.getPlugin());
  }

  @Subscribe(order = PostOrder.EARLY)
  public EventTask banCheck(LoginEvent event) {
    return EventTask.async(() -> this.checkBanJoin(event));
  }

  public void checkBanJoin(final LoginEvent event) {
    listener.banCheck(event.getPlayer().getUniqueId(), event.getPlayer().getUsername(), IPUtils.toIPAddress(event.getPlayer().getRemoteAddress().getAddress()), new BanJoinHandler(event));

    if (event.getResult().isAllowed()) {
      listener.onPreJoin(event.getPlayer().getUniqueId(), event.getPlayer().getUsername(), IPUtils.toIPAddress(event.getPlayer().getRemoteAddress().getAddress()));
    }
  }

  @Subscribe
  public void onJoin(PostLoginEvent event) {
    listener.onJoin(new VelocityPlayer(event.getPlayer(), plugin.getPlugin().getConfig().isOnlineMode()));
  }

  @Subscribe(order = PostOrder.LAST)
  public void onPlayerLogin(PostLoginEvent event) {
    listener.onPlayerLogin(new VelocityPlayer(event.getPlayer(), plugin.getPlugin().getConfig().isOnlineMode()), new LoginHandler(event));
  }

  @RequiredArgsConstructor
  public static class BanJoinHandler implements CommonJoinHandler {
    private final LoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.setResult(ResultedEvent.ComponentResult.denied((Component) VelocityServer.formatMessage(message.toString())));
    }
  }

  @RequiredArgsConstructor
  public class LoginHandler implements CommonJoinHandler {
    private final PostLoginEvent event;

    @Override
    public void handleDeny(Message message) {
      event.getPlayer().disconnect((Component) VelocityServer.formatMessage(message.toString()));
    }
  }
}
