package me.confuser.banmanager.velocity;


import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.util.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class VelocityServer implements CommonServer {
  private BanManagerPlugin plugin;
  private ProxyServer server;

  public void enable(BanManagerPlugin plugin, ProxyServer server) {this.plugin = plugin; this.server = server; }

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    Optional<Player> player = server.getPlayer(uniqueId);

    if (player.isPresent()) return new VelocityPlayer(plugin, player.get(), plugin.getConfig().isOnlineMode());

    return null;
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    Optional<Player> player = server.getPlayer(name);

    if (player.isPresent()) return new VelocityPlayer(plugin, player.get(), plugin.getConfig().isOnlineMode());

    return null;
  }

  @Override
  public CommonPlayer getPlayerExact(String name) {
    return getPlayer(name);
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
      return server.getAllPlayers().stream()
          .map(player -> new VelocityPlayer(plugin, player, plugin.getConfig().isOnlineMode()))
          .collect(Collectors.toList()).toArray(new CommonPlayer[0]);
  }

  @Override
  public void broadcast(String message, String permission) {
    if(message.isEmpty()) return;

    for (Player player : server.getAllPlayers()) {
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
    return new VelocitySender(plugin, server.getConsoleCommandSource());
  }

  @Override
  public boolean dispatchCommand(CommonSender sender, String command) {
    CommandSource velocitySender;
    if (sender.isConsole()) {
      velocitySender = server.getConsoleCommandSource();
    } else {
      if (server.getPlayer(sender.getName()).isPresent()) {
        velocitySender = server.getPlayer(sender.getName()).get();
      } else {
        return false;
      }
    }

     server.getCommandManager().executeImmediatelyAsync(velocitySender, command);
    return true;
  }

  @Override
  public CommonWorld getWorld(String name) {
    return null;
  }

  public static @NotNull Component formatMessage(String message) {
    return LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .build()
        .deserialize(ColorUtils.preprocess(message));
  }

  public static Component convert(me.confuser.banmanager.common.kyori.text.Component message) {
    String gson =  me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer.gson().serialize(message);
    return GsonComponentSerializer.gson().deserialize(gson);
  }

  @Override
  public CommonExternalCommand getPluginCommand(String commandName) {
    // This would be a implementation of doing so with Velocity, but the method getCommandMeta does not exist.
    CommandMeta meta = server.getCommandManager().getCommandMeta(commandName);
    if (meta != null) {
      return new CommonExternalCommand(null, meta.getAliases().iterator().next(), new ArrayList<>(meta.getAliases()));
    }
    else return null;
  }
}
