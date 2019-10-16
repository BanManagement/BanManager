package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.bukkit.api.events.*;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class BukkitServer implements CommonServer {

  private BanManagerPlugin plugin;

  public BukkitServer() {
  }

  public void enable(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    Player player = Bukkit.getPlayer(uniqueId);

    if (player == null) return null;

    return new BukkitPlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    Player player = Bukkit.getPlayer(name);

    if (player == null) return null;

    return new BukkitPlayer(player, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return (CommonPlayer[]) Bukkit.getOnlinePlayers().stream()
                                  .map(player -> new BukkitPlayer(player, plugin.getConfig().isOnlineMode()))
                                  .collect(Collectors.toList()).toArray();
  }

  @Override
  public void broadcast(String message, String permission) {
    Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions("bukkit.broadcast.user");

    for (Permissible permissible : permissibles) {
      if (!(permissible instanceof BlockCommandSender) && (permissible instanceof CommandSender) && permissible
              .hasPermission(permission)) {
        CommandSender user = (CommandSender) permissible;
        user.sendMessage(message);
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
    return new BukkitSender(plugin, Bukkit.getServer().getConsoleSender());
  }

  @Override
  public boolean dispatchCommand(CommonSender sender, String command) {
    CommandSender bukkitSender;

    if (sender.isConsole()) {
      bukkitSender = Bukkit.getServer().getConsoleSender();
    } else {
      bukkitSender = Bukkit.getPlayer(sender.getName());
    }

    return Bukkit.dispatchCommand(bukkitSender, command);
  }

  @Override
  public CommonWorld getWorld(String name) {
    World world = Bukkit.getWorld(name);

    if (world == null) return null;

    return new CommonWorld(name);
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

    Bukkit.getServer().getPluginManager().callEvent(event);

    if (event instanceof SilentCancellableEvent) {
      commonEvent = new CommonEvent(((SilentCancellableEvent) event).isCancelled(),((SilentCancellableEvent) event).isSilent());
    } else if (event instanceof SilentEvent) {
      commonEvent = new CommonEvent(false, ((SilentEvent) event).isSilent());
    } else if (event instanceof CustomCancellableEvent) {
      commonEvent = new CommonEvent(((CustomCancellableEvent) event).isCancelled(), true);
    }

    return commonEvent;
  }
}
