package me.confuser.banmanager.fabric;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;
import me.confuser.banmanager.common.util.MessageRegistry;
import me.confuser.banmanager.common.util.UUIDUtils;
//? if >=1.21.1
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import me.lucko.fabric.api.permissions.v0.Permissions;

public class FabricPlayer implements CommonPlayer {
  private final UUID uuid;
  private final boolean onlineMode;
  private final ServerPlayerEntity player;
  private final MinecraftServer server;

  public FabricPlayer(ServerPlayerEntity player, MinecraftServer server, boolean onlineMode) {
    this.player = player;
    this.server = server;
    this.uuid = player.getUuid();
    this.onlineMode = onlineMode;
  }

  public void kick(String message) {
    this.player.networkHandler.disconnect(FabricServer.formatMessage(message));
  }

  @Override
  public void kick(Component component) {
    this.player.networkHandler.disconnect(FabricServer.formatJsonMessage(MessageRenderer.getInstance().toJson(component)));
  }

  public void sendMessage(String message) {
    if(message.isEmpty()) return;

    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      this.player.sendMessage(FabricServer.formatMessage(message));
    }
  }

  @Override
  public void sendMessage(Component component) {
    getPlayer().sendMessage(FabricServer.formatJsonMessage(MessageRenderer.getInstance().toJson(component)));
  }

  @Override
  public void sendActionBar(Component component) {
    getPlayer().sendMessage(FabricServer.formatJsonMessage(MessageRenderer.getInstance().toJson(component)), true);
  }

  @Override
  public void showTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
    ServerPlayerEntity p = getPlayer();
    if (p == null) return;
    MessageRenderer renderer = MessageRenderer.getInstance();
    if (title != null) {
      net.minecraft.text.Text titleText = FabricServer.formatJsonMessage(renderer.toJson(title));
      p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleS2CPacket(titleText));
    }
    if (subtitle != null) {
      net.minecraft.text.Text subtitleText = FabricServer.formatJsonMessage(renderer.toJson(subtitle));
      p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.SubtitleS2CPacket(subtitleText));
    }
    p.networkHandler.sendPacket(new net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket(fadeIn, stay, fadeOut));
  }

  @Override
  public void playSound(String sound, float volume, float pitch) {
    ServerPlayerEntity p = getPlayer();
    if (p == null) return;
    try {
      //? if >=1.21 {
      net.minecraft.util.Identifier id = net.minecraft.util.Identifier.of(sound);
      //?} else {
      /*net.minecraft.util.Identifier id = new net.minecraft.util.Identifier(sound);
      *///?}
      net.minecraft.sound.SoundEvent event = net.minecraft.sound.SoundEvent.of(id);
      //? if >=1.21.11 {
      p.getEntityWorld().playSound(null, p.getBlockPos(), event, net.minecraft.sound.SoundCategory.MASTER, volume, pitch);
      //?} else {
      /*p.getWorld().playSound(null, p.getBlockPos(), event, net.minecraft.sound.SoundCategory.MASTER, volume, pitch);
      *///?}
    } catch (IllegalArgumentException ignored) {
      // Invalid sound key or identifier -- silently ignore
    }
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    getPlayer().sendMessage(FabricServer.formatMessage(jsonString));
  }

  @Override
  public void sendJSONMessage(String message) {
    getPlayer().sendMessage(FabricServer.formatJsonMessage(message));
  }

  public boolean isConsole() {
    return false;
  }

  public PlayerData getData() {
    try {
      return BanManagerPlugin.getInstance().getPlayerStorage().queryForId(UUIDUtils.toBytes(getUniqueId()));
    } catch (SQLException e) {
      BanManagerPlugin.getInstance().getLogger().warning("Failed to load player data", e);
      sendMessage(Message.get("sender.error.exception").toString());
      return null;
    }
  }

  public boolean isOnlineMode() {
    return onlineMode;
  }

  public boolean hasPermission(String permission) {
    return Permissions.check(getPlayer(), permission,4);
  }

  public String getDisplayName() {
    return getPlayer().getDisplayName().toString();
  }

  public String getName() {
    //? if >=1.21 {
    return getPlayer().getNameForScoreboard();
    //?} else {
    /*return getPlayer().getName().getString();
    *///?}
  }

  public InetAddress getAddress() {
    return ((InetSocketAddress) getPlayer().networkHandler.getConnectionAddress()).getAddress();
  }

  public UUID getUniqueId() {
    return getPlayer().getUuid();
  }

  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    ServerPlayerEntity player = getPlayer();

    for (ServerWorld world1 : this.server.getWorlds()) {
      if (world1.getRegistryKey().getValue().toString().equals(world.getName())) {
        //? if >=1.21.4 {
        return player.teleport(world1, x, y, z, Collections.<PositionFlag>emptySet(), yaw, pitch, true);
        //?} elif >=1.21.1 {
        /*return player.teleport(world1, x, y, z, Collections.<PositionFlag>emptySet(), yaw, pitch);
        *///?} else {
        /*player.teleport(world1, x, y, z, yaw, pitch);
        return true;
        *///?}
      }
    }

    return false;
  }

  @Override
  public boolean canSee(CommonPlayer player) {
    return getPlayer().canSee(this.server.getPlayerManager().getPlayer(player.getUniqueId()));
  }

  public boolean isOnline() {
    return getPlayer() != null && !getPlayer().isDisconnected();
  }

  @Override
  public String getLocale() {
    ServerPlayerEntity p = getPlayer();
    if (p == null) return "en";
    //? if >=1.21 {
    return MessageRegistry.normaliseLocale(p.getClientOptions().language());
    //?} else {
    /*// No public API for client language pre-1.21; fall back to default
    return "en";
    *///?}
  }

  private ServerPlayerEntity getPlayer() {
    if (player != null) return player;
    if (isOnlineMode()) return this.server.getPlayerManager().getPlayer(uuid);

    for (ServerPlayerEntity onlinePlayer : this.server.getPlayerManager().getPlayerList()) {
      if (UUIDUtils.createOfflineUUID(onlinePlayer.getName().toString()).equals(uuid)) return onlinePlayer;
    }

    return null;
  }

}
