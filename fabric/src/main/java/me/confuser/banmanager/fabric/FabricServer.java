package me.confuser.banmanager.fabric;

import java.util.Arrays;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.JsonOps;

import lombok.Getter;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.google.gson.JsonParser;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonExternalCommand;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.IpBanData;
import me.confuser.banmanager.common.data.IpMuteData;
import me.confuser.banmanager.common.data.IpRangeBanData;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerKickData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.data.PlayerReportData;
import me.confuser.banmanager.common.data.PlayerWarnData;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.kyori.text.serializer.legacy.LegacyComponentSerializer;
import com.google.gson.JsonElement;
import me.confuser.banmanager.common.util.Message;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class FabricServer implements CommonServer {

  private BanManagerPlugin plugin;
  @Getter
  private MinecraftServer server;

  public FabricServer() {
  }

  public void enable(BanManagerPlugin plugin, MinecraftServer server) {
    this.plugin = plugin;
    this.server = server;
  }

  public CommonPlayer getPlayer(UUID uniqueId) {
    ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(uniqueId);

    if (player == null) return null;

    return new FabricPlayer(player, this.server, plugin.getConfig().isOnlineMode());
  }

  public CommonPlayer getPlayer(String name) {
    ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(name);

    if (player == null) return null;

    return new FabricPlayer(player, this.server, plugin.getConfig().isOnlineMode());
  }

  public CommonPlayer[] getOnlinePlayers() {
    return this.server.getPlayerManager().getPlayerList().stream()
      .map(player -> new FabricPlayer(player, this.server, plugin.getConfig().isOnlineMode()))
      .filter(player -> player != null && player.isOnline())
      .toArray(CommonPlayer[]::new);
  }

  public void broadcast(String message, String permission) {
    Arrays.stream(getOnlinePlayers())
      .filter(player -> player.hasPermission(permission))
      .forEach(player -> player.sendMessage(message));
  }

  public void broadcastJSON(TextComponent message, String permission) {
    Arrays.stream(getOnlinePlayers())
      .filter(player -> player.hasPermission(permission))
      .forEach(player -> player.sendJSONMessage(message));
  }

  public void broadcast(String message, String permission, CommonSender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
  }

  public CommonSender getConsoleSender() {
    return new FabricSender(plugin, this.server.getCommandSource());
  }

  public boolean dispatchCommand(CommonSender consoleSender, String command) {
    this.server.getCommandManager().executeWithPrefix(this.server.getCommandSource(), command);

    return true;
  }

  public CommonWorld getWorld(String name) {
    for (ServerWorld world : this.server.getWorlds()) {
      if (world.getRegistryKey().getValue().toString().equals(name)) {
        return new CommonWorld(world.getRegistryKey().getValue().toString());
      }
    }

    return null;
  }

  public CommonEvent callEvent(String name, Object... args) {
    CommonEvent commonEvent;
    BanManagerEvents.SilentValue silentValue = new BanManagerEvents.SilentValue(true);

    switch (name) {
      case "PlayerBanEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.PLAYER_BAN_EVENT.invoker().onPlayerBan((PlayerBanData) args[0], silentValue), silentValue.isSilent());
        break;
      case "PlayerBannedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.PLAYER_BANNED_EVENT.invoker().onPlayerBanned((PlayerBanData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;
      case "PlayerUnbanEvent":
        BanManagerEvents.PLAYER_UNBAN_EVENT.invoker().onPlayerUnban((PlayerBanData) args[0], (PlayerData) args[1], (String) args[2]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "IpBanEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.IP_BAN_EVENT.invoker().onIpBan((IpBanData) args[0], silentValue), silentValue.isSilent());
        break;
      case "IpBannedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.IP_BANNED_EVENT.invoker().onIpBanned((IpBanData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;
      case "IpUnbanEvent":
        BanManagerEvents.IP_UNBAN_EVENT.invoker().onIpUnban((IpBanData) args[0], (PlayerData) args[1], (String) args[2]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "IpMuteEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.IP_MUTE_EVENT.invoker().onIpMute((IpMuteData) args[0], silentValue), silentValue.isSilent());
        break;
      case "IpMutedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.IP_MUTED_EVENT.invoker().onIpMuted((IpMuteData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;
      case "IpUnmutedEvent":
        BanManagerEvents.IP_UNMUTED_EVENT.invoker().onIpUnmuted((IpMuteData) args[0], (PlayerData) args[1], (String) args[2]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "PlayerKickedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.PLAYER_KICKED_EVENT.invoker().onPlayerKicked((PlayerKickData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;

      case "PlayerNoteCreatedEvent":
        BanManagerEvents.PLAYER_NOTE_CREATED_EVENT.invoker().onPlayerNoteCreated((PlayerNoteData) args[0]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "PlayerReportEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.PLAYER_REPORT_EVENT.invoker().onPlayerReport((PlayerReportData) args[0], silentValue), silentValue.isSilent());
        break;
      case "PlayerReportedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.PLAYER_REPORTED_EVENT.invoker().onPlayerReported((PlayerReportData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;
      case "PlayerReportDeletedEvent":
        BanManagerEvents.PLAYER_REPORT_DELETED_EVENT.invoker().onPlayerReportDeleted((PlayerReportData) args[0]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "NameBanEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.NAME_BAN_EVENT.invoker().onNameBan((NameBanData) args[0], silentValue), silentValue.isSilent());
        break;
      case "NameBannedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.NAME_BANNED_EVENT.invoker().onNameBanned((NameBanData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;
      case "NameUnbanEvent":
        BanManagerEvents.NAME_UNBAN_EVENT.invoker().onNameUnban((NameBanData) args[0], (PlayerData) args[1], (String) args[2]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "PlayerWarnEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.PLAYER_WARN_EVENT.invoker().onPlayerWarn((PlayerWarnData) args[0], silentValue), silentValue.isSilent());
        break;
      case "PlayerWarnedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.PLAYER_WARNED_EVENT.invoker().onPlayerWarned((PlayerWarnData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;

      case "IpRangeBanEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.IP_RANGE_BAN_EVENT.invoker().onIpRangeBan((IpRangeBanData) args[0], silentValue), silentValue.isSilent());
        break;
      case "IpRangeBannedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.IP_RANGE_BANNED_EVENT.invoker().onIpRangeBanned((IpRangeBanData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;
      case "IpRangeUnbanEvent":
        BanManagerEvents.IP_RANGE_UNBAN_EVENT.invoker().onIpRangeUnban((IpRangeBanData) args[0], (PlayerData) args[1], (String) args[2]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "PlayerMuteEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        commonEvent = new CommonEvent(!BanManagerEvents.PLAYER_MUTE_EVENT.invoker().onPlayerMute((PlayerMuteData) args[0], silentValue), silentValue.isSilent());
        break;
      case "PlayerMutedEvent":
        silentValue = new BanManagerEvents.SilentValue((boolean) args[1]);
        BanManagerEvents.PLAYER_MUTED_EVENT.invoker().onPlayerMuted((PlayerMuteData) args[0], silentValue.isSilent());
        commonEvent = new CommonEvent(false, silentValue.isSilent());
        break;
      case "PlayerUnmuteEvent":
        BanManagerEvents.PLAYER_UNMUTE_EVENT.invoker().onPlayerUnmute((PlayerMuteData) args[0], (PlayerData) args[1], (String) args[2]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "PluginReloadedEvent":
        BanManagerEvents.PLUGIN_RELOADED_EVENT.invoker().onPluginReloaded((PlayerData) args[0]);
        commonEvent = new CommonEvent(false, false);
        break;

      case "PlayerDeniedEvent":
        BanManagerEvents.PLAYER_DENIED_EVENT.invoker().onPlayerDenied((PlayerData) args[0], (Message) args[1]);
        commonEvent = new CommonEvent(false, false);
        break;

      default:
        commonEvent = new CommonEvent(false, false);
        break;
    }

    return commonEvent;
  }

  public CommonExternalCommand getPluginCommand(String commandName) {
    for (CommandNode<?> commandNode : this.server.getCommandManager().getDispatcher().getRoot().getChildren()) {
      if (commandNode.getName().equals(commandName)) {
        List<String> redirects = new ArrayList<>();

        if (commandNode instanceof LiteralCommandNode) {
          LiteralCommandNode<?> literalNode = (LiteralCommandNode<?>) commandNode;
          CommandNode<?> redirectNode = literalNode.getRedirect();

          if (redirectNode != null) {
            redirects.add(redirectNode.getName());
          }
        }

        return new CommonExternalCommand(commandNode.getName(), commandNode.getName(), redirects);
      }
    }
    return null;
  }

  public static Text formatMessage(String message) {
    return formatMessage(LegacyComponentSerializer.legacy('&').deserialize(message));
  }

  public static Text formatMessage(Message message) {
    return formatMessage(message.toString());
  }

  public static Text formatMessage(TextComponent message) {
    return TextCodecs.CODEC
      .decode(JsonOps.INSTANCE, JsonParser.parseString(GsonComponentSerializer.gson().serialize(message)))
      .getOrThrow()
      .getFirst();
  }

  public static Text formatJsonMessage(String message) {
    return TextCodecs.CODEC
      .decode(JsonOps.INSTANCE, JsonParser.parseString(message))
      .getOrThrow()
      .getFirst();
  }
}
