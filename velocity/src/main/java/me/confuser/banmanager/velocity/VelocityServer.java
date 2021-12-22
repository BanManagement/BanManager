package me.confuser.banmanager.velocity;



import me.confuser.banmanager.velocity.api.events.*;
import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.*;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.ComponentSerializer;
import net.kyori.text.TextComponent;


import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;


import java.util.Arrays;
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

    if (player == null) return null;

    return new VelocityPlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    Optional<Player> player = server.getPlayer(name);

    if (player == null) return null;

    return new VelocityPlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
      return server.getAllPlayers().stream()
          .map(player -> new VelocityPlayer(Optional.of(player), plugin.getConfig().isOnlineMode()))
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
    for (Player player : server.getAllPlayers()) {
      if (player != null && player.hasPermission(permission)) {
        player.sendMessage((Component) message);
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
    //@TODO MAKE SURE THIS DOESNT BREAK STUFF
    if (sender.isConsole()) {
      velocitySender = server.getConsoleCommandSource();
    } else {
      velocitySender = server.getPlayer(sender.getName()).get();
    }

     server.getCommandManager().executeImmediatelyAsync(velocitySender, command);
    return true; // bro what do you WANT FROMMME
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
    }

    if (event == null) {
      plugin.getLogger().warning("Unable to call missing event " + name);

      return commonEvent;
    }

    server.getEventManager().fire(event);

    if (event instanceof SilentCancellableEvent) {
      commonEvent = new CommonEvent(((SilentCancellableEvent) event).isCancelled(), ((SilentCancellableEvent) event).isSilent());
    } else if (event instanceof SilentEvent) {
      commonEvent = new CommonEvent(false, ((SilentEvent) event).isSilent());
    } else if (event instanceof CustomCancellableEvent) {
      commonEvent = new CommonEvent(((CustomCancellableEvent) event).isCancelled(), true);
    }

    return commonEvent;
  }

  public static Component formatMessage(String message) {
    return (Component) LegacyComponentSerializer.legacy().deserialize(message);
  }

  public static TextComponent formatMessage(TextComponent message) {
    return message;
  }

  @Override
  public CommonExternalCommand getPluginCommand(String commandName) {
    // @TODO Seems like Velocity doesn't expose an easy way to retrieve a command by name?
//      server.getCommandManager().getCommandMeta(commandName);

    return null;

//    return new CommonExternalCommand(null, command.getValue().getName(), Arrays.asList(command.getValue().getAliases()));
  }
}



