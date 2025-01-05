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
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
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

  public void sendMessage(String message) {
    if(message.isEmpty()) return;

    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      this.player.sendMessage(FabricServer.formatMessage(message));
    }
  }

  public void sendMessage(Message message) {
    sendMessage(message.toString());
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
      e.printStackTrace();
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
    return getPlayer().getNameForScoreboard();
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
        return player.teleport(world1, x, y, z, Collections.<PositionFlag>emptySet(), yaw, pitch, true);
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

  private ServerPlayerEntity getPlayer() {
    if (player != null) return player;
    if (isOnlineMode()) return this.server.getPlayerManager().getPlayer(uuid);

    for (ServerPlayerEntity onlinePlayer : this.server.getPlayerManager().getPlayerList()) {
      if (UUIDUtils.createOfflineUUID(onlinePlayer.getName().toString()).equals(uuid)) return onlinePlayer;
    }

    return null;
  }

}
