package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.util.Message;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;

import java.net.InetAddress;
import java.util.UUID;

public class BungeePlayer implements CommonPlayer {
  private final UUID uuid;
  private final boolean onlineMode;
  private final ProxiedPlayer player;

  public BungeePlayer(ProxiedPlayer player, boolean onlineMode) {
    this.player = player;
    this.uuid = player.getUniqueId();
    this.onlineMode = onlineMode;
  }

  @Override
  public void kick(String message) {
    player.disconnect(BungeeServer.formatMessage(message));
  }

  @Override
  public void sendMessage(String message) {
    if(message.isEmpty()) return;

    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      player.sendMessage(BungeeServer.formatMessage(message));
    }
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    player.sendMessage(BungeeServer.formatMessage(jsonString));
  }

  @Override
  public void sendJSONMessage(String jsonString) {
    player.sendMessage(ComponentSerializer.parse(jsonString));
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
    return player.hasPermission(permission);
  }

  @Override
  public String getDisplayName() {
    return player.getDisplayName();
  }

  @Override
  public String getName() {
    return player.getName();
  }

  @Override
  public InetAddress getAddress() {
    return player.getAddress().getAddress();
  }

  @Override
  public UUID getUniqueId() {
    return uuid;
  }

  @Override
  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }

  @Override
  public boolean canSee(CommonPlayer player) {
    return true;
  }

  private ProxiedPlayer getPlayer() {
    return ProxyServer.getInstance().getPlayer(uuid);
  }
}
