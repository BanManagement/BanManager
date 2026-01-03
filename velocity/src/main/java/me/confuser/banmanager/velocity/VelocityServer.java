package me.confuser.banmanager.velocity;


import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.util.ColorUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.velocity.api.events.*;
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

    if (player.isPresent()) return new VelocityPlayer(player.get(), plugin.getConfig().isOnlineMode());

    return null;
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    Optional<Player> player = server.getPlayer(name);

    if (player.isPresent()) return new VelocityPlayer(player.get(), plugin.getConfig().isOnlineMode());

    return null;
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
      return server.getAllPlayers().stream()
          .map(player -> new VelocityPlayer(player, plugin.getConfig().isOnlineMode()))
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
  public void broadcastJSON(TextComponent message, String permission) {
    Component converted = convert(message);
    for (Player player : server.getAllPlayers()) {
      if (player.hasPermission(permission)) {
        player.sendMessage(converted);
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

  @Override
  public CommonEvent callEvent(String name, Object... args) {
    // @TODO replace with a cleaner implementation
    CustomEvent event = null;
    CommonEvent commonEvent = new CommonEvent(false, true);

    switch (name) {
      case "PlayerBanEvent":
        event = new PlayerBanEvent((PlayerBanData) args[0], (boolean) args[1]);
        break;
      case "PlayerBannedEvent":
        event = new PlayerBannedEvent((PlayerBanData) args[0], (boolean) args[1]);
        break;
      case "PlayerUnbanEvent":
        event = new PlayerUnbanEvent((PlayerBanData) args[0], (PlayerData) args[1], (String) args[2]);
        break;

      case "IpBanEvent":
        event = new IpBanEvent((IpBanData) args[0], (boolean) args[1]);
        break;
      case "IpBannedEvent":
        event = new IpBannedEvent((IpBanData) args[0], (boolean) args[1]);
        break;
      case "IpUnbanEvent":
        event = new IpUnbanEvent((IpBanData) args[0], (PlayerData) args[1], (String) args[2]);
        break;

      case "IpMuteEvent":
        event = new IpMuteEvent((IpMuteData) args[0], (boolean) args[1]);
        break;
      case "IpMutedEvent":
        event = new IpMutedEvent((IpMuteData) args[0], (boolean) args[1]);
        break;
      case "IpUnmutedEvent":
        event = new IpUnmutedEvent((IpMuteData) args[0], (PlayerData) args[1], (String) args[2]);
        break;

      case "PlayerKickedEvent":
        event = new PlayerKickedEvent((PlayerKickData) args[0], (boolean) args[1]);
        break;

      case "PlayerNoteCreatedEvent":
        event = new PlayerNoteCreatedEvent((PlayerNoteData) args[0]);
        break;

      case "PlayerReportEvent":
        event = new PlayerReportEvent((PlayerReportData) args[0], (boolean) args[1]);
        break;
      case "PlayerReportedEvent":
        event = new PlayerReportedEvent((PlayerReportData) args[0], (boolean) args[1]);
        break;
      case "PlayerReportDeletedEvent":
        event = new PlayerReportDeletedEvent((PlayerReportData) args[0]);
        break;

      case "NameBanEvent":
        event = new NameBanEvent((NameBanData) args[0], (boolean) args[1]);
        break;
      case "NameBannedEvent":
        event = new NameBannedEvent((NameBanData) args[0], (boolean) args[1]);
        break;
      case "NameUnbanEvent":
        event = new NameUnbanEvent((NameBanData) args[0], (PlayerData) args[1], (String) args[2]);
        break;

      case "PlayerWarnEvent":
        event = new PlayerWarnEvent((PlayerWarnData) args[0], (boolean) args[1]);
        break;
      case "PlayerWarnedEvent":
        event = new PlayerWarnedEvent((PlayerWarnData) args[0], (boolean) args[1]);
        break;

      case "IpRangeBanEvent":
        event = new IpRangeBanEvent((IpRangeBanData) args[0], (boolean) args[1]);
        break;
      case "IpRangeBannedEvent":
        event = new IpRangeBannedEvent((IpRangeBanData) args[0], (boolean) args[1]);
        break;
      case "IpRangeUnbanEvent":
        event = new IpRangeUnbanEvent((IpRangeBanData) args[0], (PlayerData) args[1], (String) args[2]);
        break;

      case "PlayerMuteEvent":
        event = new PlayerMuteEvent((PlayerMuteData) args[0], (boolean) args[1]);
        break;
      case "PlayerMutedEvent":
        event = new PlayerMutedEvent((PlayerMuteData) args[0], (boolean) args[1]);
        break;
      case "PlayerUnmuteEvent":
        event = new PlayerUnmuteEvent((PlayerMuteData) args[0], (PlayerData) args[1], (String) args[2]);
        break;

      case "PluginReloadedEvent":
        event = new PluginReloadedEvent((PlayerData) args[0]);
        break;

      case "PlayerDeniedEvent":
        event = new PlayerDeniedEvent((PlayerData) args[0], (Message) args[1]);
    }

    if (event == null) {
      plugin.getLogger().warning("Unable to call missing event " + name);

      return commonEvent;
    }

    server.getEventManager().fire(event).join();

    if (event instanceof SilentCancellableEvent) {
      commonEvent = new CommonEvent(!(((SilentCancellableEvent) event).getResult().isAllowed()), ((SilentCancellableEvent) event).isSilent());
    } else if (event instanceof SilentEvent) {
      commonEvent = new CommonEvent(false, ((SilentEvent) event).isSilent());
    } else if (event instanceof CustomCancellableEvent) {
      commonEvent = new CommonEvent(!((CustomCancellableEvent) event).getResult().isAllowed(), true);
    }

    return commonEvent;
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
