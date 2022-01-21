package me.confuser.banmanager.velocity;

import com.velocitypowered.api.proxy.Player;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.TextComponent;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

public class VelocityPlayer implements CommonPlayer {
  private final UUID uuid;
  private final boolean onlineMode;
  public VelocityPlayer(Optional<Player> player, boolean onlineMode) {
    this.uuid = player.get().getUniqueId();
    this.onlineMode = onlineMode;
  }

  @Override
  public void kick(String message) {
    getPlayer().get().disconnect(VelocityServer.formatMessage(message));
  }

  @Override
  public void sendMessage(String message) {
    if(message.isEmpty()) return;

    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      getPlayer().get().sendMessage(VelocityServer.formatMessage(message));
    }
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    // @TODO Find a fix for the JSON component
    getPlayer().get().sendMessage(VelocityServer.formatMessage(jsonString.content()));
  }

  @Override
  public void sendJSONMessage(String jsonString) {
    getPlayer().get().sendMessage(GsonComponentSerializer.colorDownsamplingGson().deserialize(jsonString));
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
    return getPlayer().isPresent();
  }

  @Override
  public boolean hasPermission(String permission) {
    return getPlayer().get().hasPermission(permission);
  }
  // @TODO Velocity doesn't seem to support display names?
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Override
  public String getName() {
    return getPlayer().get().getUsername();
  }

  @Override
  public InetAddress getAddress() {
    if(getPlayer().isPresent()) {
      return getPlayer().get().getRemoteAddress().getAddress();
    }
    return null;
  }

  @Override
  public UUID getUniqueId() {
    if(getPlayer().isPresent()) {
      return getPlayer().get().getUniqueId();
    }
    return null;
  }

  @Override
  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }

  @Override
  public boolean canSee(CommonPlayer player) {
    return true;
  }

  private Optional<Player> getPlayer() {
    return BMVelocityPlugin.getInstance().server.getPlayer(uuid);
  }
}
