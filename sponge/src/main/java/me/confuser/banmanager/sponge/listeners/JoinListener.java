package me.confuser.banmanager.sponge.listeners;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.j256.ormlite.dao.CloseableIterator;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CountryResponse;
import inet.ipaddr.IPAddress;
import lombok.RequiredArgsConstructor;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.commands.NotesCommand;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.listeners.CommonJoinHandler;
import me.confuser.banmanager.common.listeners.CommonJoinListener;
import me.confuser.banmanager.common.util.*;
import me.confuser.banmanager.sponge.SpongePlayer;
import me.confuser.banmanager.sponge.SpongeServer;
import org.apache.commons.lang3.time.FastDateFormat;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinListener {
  private final CommonJoinListener listener;
  private BanManagerPlugin plugin;

  public JoinListener(BanManagerPlugin plugin) {
    this.plugin = plugin;
    this.listener = new CommonJoinListener(plugin);
  }

  @Listener(order = Order.LAST)
  public void banCheck(final ClientConnectionEvent.Auth event) {
    InetAddress address = event.getConnection().getAddress().getAddress();
    String name = event.getProfile().getName().get();

    listener.banCheck(event.getProfile().getUniqueId(), name, IPUtils.toIPAddress(address), new BanJoinHandler(event));
  }

  @Listener(order = Order.LAST)
  public void onJoin(ClientConnectionEvent.Auth event) {
    this.listener.onPreJoin(event.getProfile().getUniqueId(), event.getProfile().getName().get(), IPUtils.toIPAddress(event.getConnection().getAddress().getAddress()));
  }

  @Listener(order = Order.LAST)
  public void onJoin(final ClientConnectionEvent.Join event) {
    listener.onJoin(new SpongePlayer(event.getTargetEntity(), plugin.getConfig().isOnlineMode()));
  }

  @Listener(order = Order.LAST)
  public void onPlayerLogin(final ClientConnectionEvent.Login event) {
    User user = event.getTargetUser();
    listener.onPlayerLogin(new SpongePlayer(user.getUniqueId(), user.getName(), plugin.getConfig().isOnlineMode()), new LoginHandler(event));
  }

  @RequiredArgsConstructor
  private class BanJoinHandler implements CommonJoinHandler {
    private final ClientConnectionEvent.Auth event;

    @Override
    public void handleDeny(Message message) {
      event.setCancelled(true);
      event.setMessage(SpongeServer.formatMessage(message.toString()));
    }
  }

  @RequiredArgsConstructor
  private class LoginHandler implements CommonJoinHandler {
    private final ClientConnectionEvent.Login event;

    @Override
    public void handleDeny(Message message) {
      event.setMessage(SpongeServer.formatMessage(message.toString()));
      event.setCancelled(true);
    }
  }
}