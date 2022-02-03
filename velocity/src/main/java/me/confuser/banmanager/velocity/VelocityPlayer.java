package me.confuser.banmanager.velocity;

import com.velocitypowered.api.proxy.Player;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;


import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

public class VelocityPlayer implements CommonPlayer {
  private final UUID uuid;
  private final boolean onlineMode;
  private final Player player;

  public VelocityPlayer(Player player, boolean onlineMode) {
    this.player = player;
    this.uuid = this.player.getUniqueId();
    this.onlineMode = onlineMode;
  }

  @Override
  public void kick(String message) {
    player.disconnect(VelocityServer.formatMessage(message));
  }

  @Override
  public void sendMessage(String message) {
    if(message.isEmpty()) return;

    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      player.sendMessage(VelocityServer.formatMessage(message));
    }
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    player.sendMessage(VelocityServer.convert(jsonString));
  }

  @Override
  public void sendJSONMessage(String jsonString) {
    player.sendMessage(GsonComponentSerializer.colorDownsamplingGson().deserialize(jsonString));
  }

  @Override
  public boolean isConsole() {
    return false;
  }

  @Override
  public PlayerData getData() {
    return CommonCommand.getPlayer(this, getName(), false);
  }

  @Override
  public boolean isOnlineMode() {
    return onlineMode;
  }

  @Override
  public boolean isOnline() {
    return this.player != null;
  }

  @Override
  public boolean hasPermission(String permission) {
    return this.player.hasPermission(permission);
  }

  @Override
  public String getDisplayName() {
    return getName();
  }

  @Override
  public String getName() {
    return this.player.getUsername();
  }

  @Override
  public InetAddress getAddress() {
    return this.player.getRemoteAddress().getAddress();
  }

  @Override
  public UUID getUniqueId() {
    return this.player.getUniqueId();
  }

  @Override
  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }

  @Override
  public boolean canSee(CommonPlayer player) {
    return true;
  }
}
