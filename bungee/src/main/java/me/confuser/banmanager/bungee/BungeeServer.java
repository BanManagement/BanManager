package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.util.ColorUtils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Arrays;
import java.util.Map;
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

    return new BungeePlayer(plugin, player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    ProxiedPlayer player = ProxyServer.getInstance().getPlayer(name);

    if (player == null) return null;

    return new BungeePlayer(plugin, player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayerExact(String name) {
    return getPlayer(name);
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return ProxyServer.getInstance().getPlayers().stream()
        .map(player -> new BungeePlayer(plugin, player, plugin.getConfig().isOnlineMode()))
        .collect(Collectors.toList()).toArray(new CommonPlayer[0]);
  }

  @Override
  public void broadcast(String message, String permission) {
    if(message.isEmpty()) return;

    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
      if (player != null && player.hasPermission(permission)) {
        player.sendMessage(formatMessage(message));
      }
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

  public static BaseComponent[] formatMessage(String message) {
    String json = ColorUtils.toDownsampledJson(message);
    return ComponentSerializer.parse(json);
  }

  public static BaseComponent[] formatMessage(Component message) {
    return ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(message));
  }

  @Override
  public CommonExternalCommand getPluginCommand(String commandName) {
    // @TODO Seems like BungeeCord doesn't expose an easy way to retrieve a command by name?
    Map.Entry<String, Command> command = ProxyServer.getInstance().getPluginManager().getCommands().stream()
        .filter(cmd -> cmd.getValue().getName().equals(commandName))
        .findFirst()
        .orElse(null);

    if (command == null) return null;

    return new CommonExternalCommand(null, command.getValue().getName(), Arrays.asList(command.getValue().getAliases()));
  }
}
