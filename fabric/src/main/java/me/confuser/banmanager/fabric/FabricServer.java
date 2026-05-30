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
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.kyori.text.serializer.legacy.LegacyComponentSerializer;
import com.google.gson.JsonElement;
import me.confuser.banmanager.common.util.ColorUtils;
import me.confuser.banmanager.common.util.Message;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
//? if >=1.21 {
import net.minecraft.text.TextCodecs;
//?}

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

    return new FabricPlayer(plugin, player, this.server, plugin.getConfig().isOnlineMode());
  }

  public CommonPlayer getPlayer(String name) {
    ServerPlayerEntity player = this.server.getPlayerManager().getPlayer(name);

    if (player == null) return null;

    return new FabricPlayer(plugin, player, this.server, plugin.getConfig().isOnlineMode());
  }

  @Override
  public CommonPlayer getPlayerExact(String name) {
    return getPlayer(name);
  }

  public CommonPlayer[] getOnlinePlayers() {
    return this.server.getPlayerManager().getPlayerList().stream()
      .map(player -> new FabricPlayer(plugin, player, this.server, plugin.getConfig().isOnlineMode()))
      .filter(player -> player != null && player.isOnline())
      .toArray(CommonPlayer[]::new);
  }

  public void broadcast(String message, String permission) {
    Arrays.stream(getOnlinePlayers())
      .filter(player -> player.hasPermission(permission))
      .forEach(player -> player.sendMessage(message));

    getConsoleSender().sendMessage(message);
  }

  public void broadcast(String message, String permission, CommonSender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
  }

  public CommonSender getConsoleSender() {
    return new FabricSender(plugin, this.server.getCommandSource());
  }

  public boolean dispatchCommand(CommonSender consoleSender, String command) {
    //? if >=1.21.11 {
    this.server.getCommandManager().parseAndExecute(this.server.getCommandSource(), command);
    //?} else {
    /*this.server.getCommandManager().executeWithPrefix(this.server.getCommandSource(), command);
    *///?}
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

  public CommonExternalCommand getPluginCommand(String commandName) {
    CommandNode<?> node = null;

    // Not overly efficient but it's only on startup and seems to find aliases correctly for blocking
    for (CommandNode<?> commandNode : this.server.getCommandManager().getDispatcher().getRoot().getChildren()) {
      if (commandNode.getName().equals(commandName)) {
        node = commandNode;
        break;
      }
      if (commandNode instanceof LiteralCommandNode) {
        LiteralCommandNode<?> literalNode = (LiteralCommandNode<?>) commandNode;
        for (CommandNode<?> child : literalNode.getChildren()) {
          if (child.getName().equals(commandName)) {
            node = child;
            break;
          }
        }
        if (node == null) {
          CommandNode<?> redirectNode = literalNode.getRedirect();
          if (redirectNode != null && redirectNode.getName().equals(commandName)) {
            node = redirectNode;
            break;
          }
        }
      }
    }

    if (node == null) return null;

    List<String> redirects = new ArrayList<>();

    for (CommandNode<?> commandNode : this.server.getCommandManager().getDispatcher().getRoot().getChildren()) {
      if (commandNode instanceof LiteralCommandNode) {
        LiteralCommandNode<?> literalNode = (LiteralCommandNode<?>) commandNode;
        for (CommandNode<?> child : literalNode.getChildren()) {
          if (child.getName().equals(node.getName())) {
            collectRedirects(child, redirects);
          }
        }
        CommandNode<?> redirectNode = literalNode.getRedirect();
        if (redirectNode != null && redirectNode.getName().equals(node.getName())) {
          redirects.add(literalNode.getName());

          collectRedirects(redirectNode, redirects);
        }
      }
    }

    return new CommonExternalCommand(node.getName(), node.getName(), redirects);
  }

  private void collectRedirects(CommandNode<?> node, List<String> redirects) {
    if (node instanceof LiteralCommandNode) {
      LiteralCommandNode<?> literalNode = (LiteralCommandNode<?>) node;
      CommandNode<?> redirectNode = literalNode.getRedirect();

      if (redirectNode != null) {
        redirects.add(redirectNode.getName());
        collectRedirects(redirectNode, redirects);
      }
      for (CommandNode<?> child : literalNode.getChildren()) {
        collectRedirects(child, redirects);
      }
    }
  }

  public static Text formatMessage(String message) {
    return formatMessage(LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .build()
        .deserialize(ColorUtils.preprocess(message)));
  }

  public static Text formatMessage(Message message) {
    return formatMessage(message.resolveComponent());
  }

  public static Text formatMessage(Component message) {
    //? if >=1.21 {
    return TextCodecs.CODEC
      .decode(JsonOps.INSTANCE, JsonParser.parseString(GsonComponentSerializer.gson().serialize(message)))
      .getOrThrow()
      .getFirst();
    //?} else {
    /*return Text.Serializer.fromJson(GsonComponentSerializer.gson().serialize(message));
    *///?}
  }

  public static Text formatJsonMessage(String message) {
    //? if >=1.21 {
    return TextCodecs.CODEC
      .decode(JsonOps.INSTANCE, JsonParser.parseString(message))
      .getOrThrow()
      .getFirst();
    //?} else {
    /*return Text.Serializer.fromJson(message);
    *///?}
  }
}
