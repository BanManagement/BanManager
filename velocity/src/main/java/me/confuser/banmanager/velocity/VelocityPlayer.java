package me.confuser.banmanager.velocity;

import com.velocitypowered.api.proxy.Player;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.TextComponent;

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
    getPlayer().disconnect(VelocityServer.formatMessage(message));
  }

  @Override
  public void sendMessage(String message) {
    if(message.isEmpty()) return;

    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      getPlayer().sendMessage(VelocityServer.formatMessage(message));
    }
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    // @TODO Find a fix for the JSON component
    getPlayer().sendMessage(VelocityServer.formatMessage(jsonString.content()));
  }

  @Override
  public void sendJSONMessage(String jsonString) {
    getPlayer().sendMessage(GsonComponentSerializer.colorDownsamplingGson().deserialize(jsonString));
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
    return getPlayer() != null;
  }

  @Override
  public boolean hasPermission(String permission) {
    return getPlayer().hasPermission(permission);
  }
  // @TODO Velocity doesn't seem to support display names?
  @Override
  public String getDisplayName() {
    return getName();
  }

  @Override
  public String getName() {
    return getPlayer().getUsername();
  }

  @Override
  public InetAddress getAddress() {
    return getPlayer().getRemoteAddress().getAddress();
  }

  @Override
  public UUID getUniqueId() {
    return getPlayer().getUniqueId();
  }

  @Override
  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }

  @Override
  public boolean canSee(CommonPlayer player) {
    return true;
  }

  private Player getPlayer() {
    Optional<Player> fetchedPlayer = BMVelocityPlugin.getInstance().server.getPlayer(uuid);
    return fetchedPlayer.orElse(null);
  }
}
