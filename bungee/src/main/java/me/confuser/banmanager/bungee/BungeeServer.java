package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.UUID;
import java.util.stream.Collectors;

public class BungeeServer implements CommonServer {
  private BanManagerPlugin plugin;

  public void enable(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(uniqueId);

    if (player == null) return null;

    return new BungeePlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);

    if (player == null) return null;

    return new BungeePlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return ProxyServer.getInstance().getPlayers().stream()
        .map(player -> new BungeePlayer(player, plugin.getConfig().isOnlineMode()))
        .collect(Collectors.toList()).toArray(new CommonPlayer[0]);
  }

  @Override
  public void broadcast(String message, String permission) {
    for (CommonPlayer player : getOnlinePlayers()) {
      if (player.hasPermission(permission)) player.sendMessage(message);
    }
  }

  @Override
  public void broadcastJSON(TextComponent message, String permission) {
    for (CommonPlayer player : getOnlinePlayers()) {
      if (player.hasPermission(permission)) player.sendJSONMessage(message);
    }
  }

  @Override
  public void broadcast(String message, String permission, CommonSender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
  }

  @Override
  public CommonSender getConsoleSender() {
    return new BungeeSender(plugin, ProxyServer.getInstance().getConsole());
  }

  @Override
  public boolean dispatchCommand(CommonSender sender, String command) {
    CommandSender bungeeSender;

    if (sender.isConsole()) {
      bungeeSender = ProxyServer.getInstance().getConsole();
    } else {
      bungeeSender = ProxyServer.getInstance().getPlayer(sender.getName());
    }

    return ProxyServer.getInstance().getPluginManager().dispatchCommand(bungeeSender, command);
  }

  @Override
  public CommonWorld getWorld(String name) {
    return null;
  }

  @Override
  public CommonEvent callEvent(String name, Object... args) {
    return null;
  }

  public static BaseComponent[] formatMessage(String message) {
    return ComponentSerializer.parse(message);
  }

  public static BaseComponent[] formatMessage(TextComponent message) {
    return ComponentSerializer.parse(GsonComponentSerializer.INSTANCE.serialize(message));
  }
}
