package me.confuser.banmanager.bungee;

import me.confuser.banmanager.bungee.api.events.*;
import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.*;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.api.ChatColor;
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
    for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
      if (player != null && player.hasPermission(permission)) {
        player.sendMessage(formatMessage(message));
      }
    }
  }

  @Override
  public void broadcastJSON(TextComponent message, String permission) {
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

    ProxyServer.getInstance().getPluginManager().callEvent(event);

    if (event instanceof SilentCancellableEvent) {
      commonEvent = new CommonEvent(((SilentCancellableEvent) event).isCancelled(), ((SilentCancellableEvent) event).isSilent());
    } else if (event instanceof SilentEvent) {
      commonEvent = new CommonEvent(false, ((SilentEvent) event).isSilent());
    } else if (event instanceof CustomCancellableEvent) {
      commonEvent = new CommonEvent(((CustomCancellableEvent) event).isCancelled(), true);
    }

    return commonEvent;
  }

  public static BaseComponent[] formatMessage(String message) {
    return net.md_5.bungee.api.chat.TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', message));
  }

  public static BaseComponent[] formatMessage(TextComponent message) {
    return ComponentSerializer.parse(GsonComponentSerializer.INSTANCE.serialize(message));
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



