package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.sponge.api.events.*;
import net.kyori.text.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.World;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpongeServer implements CommonServer {
  private BanManagerPlugin plugin;

  public void enable(BanManagerPlugin plugin) {
    this.plugin = plugin;
  }

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    Optional<Player> player = Sponge.getGame().getServer().getPlayer(uniqueId);

    return player.map(value -> new SpongePlayer(value, plugin.getConfig().isOnlineMode())).orElse(null);

  }

  @Override
  public CommonPlayer getPlayer(String name) {
    Optional<Player> player = Sponge.getGame().getServer().getPlayer(name);

    return player.map(value -> new SpongePlayer(value, plugin.getConfig().isOnlineMode())).orElse(null);

  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return Sponge.getServer().getOnlinePlayers().stream()
        .map(player -> new SpongePlayer(player, plugin.getConfig().isOnlineMode()))
        .collect(Collectors.toList()).toArray(new CommonPlayer[0]);
  }

  @Override
  public void broadcast(String message, String permission) {
    // @TODO can't figure out how to get message channels to work ¯\_(ツ)_/¯
    // MessageChannel.permission(permission).send(Sponge.getServer().getConsole(), Text.of(message));
    Arrays.stream(getOnlinePlayers()).forEach(player -> {
      if (player.hasPermission(permission)) player.sendMessage(message);
    });

    Sponge.getServer().getConsole().sendMessage(Text.of(message));
  }

  @Override
  public void broadcastJSON(TextComponent message, String permission) {
    Arrays.stream(getOnlinePlayers()).forEach(player -> {
      if (player.hasPermission(permission)) player.sendJSONMessage(message);
    });
  }

  public void broadcast(String message, String permission, CommonSender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
  }

  public static Text formatMessage(String message) {
    return TextSerializers.FORMATTING_CODE.deserialize(message);
  }

  public CommonSender getConsoleSender() {
    return new SpongeSender(plugin, Sponge.getServer().getConsole());
  }

  public boolean dispatchCommand(CommonSender sender, String command) {
    if (sender.isConsole()) {
      Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
    } else {
      Sponge.getCommandManager().process(Sponge.getServer().getPlayer(sender.getName()).get(), command);
    }

    return true;
  }

  public CommonWorld getWorld(String name) {
    Optional<World> world = Sponge.getServer().getWorld(name);

    if (!world.isPresent()) return null;

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

    Sponge.getEventManager().post(event);

    if (event instanceof SilentCancellableEvent) {
      commonEvent = new CommonEvent(((SilentCancellableEvent) event).isCancelled(), ((SilentCancellableEvent) event).isSilent());
    } else if (event instanceof SilentEvent) {
      commonEvent = new CommonEvent(false, ((SilentEvent) event).isSilent());
    } else if (event instanceof CustomCancellableEvent) {
      commonEvent = new CommonEvent(((CustomCancellableEvent) event).isCancelled(), true);
    }

    return commonEvent;
  }
}
