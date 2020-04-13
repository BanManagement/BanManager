package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import net.kyori.text.TextComponent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;

import java.net.InetAddress;
import java.util.UUID;

public class BungeePlayer implements CommonPlayer {
  private final UUID uuid;
  private final boolean onlineMode;

  public BungeePlayer(ProxiedPlayer player, boolean onlineMode) {
    this.uuid = player.getUniqueId();
    this.onlineMode = onlineMode;
  }

  @Override
  public void kick(String message) {
    getPlayer().disconnect(BungeeServer.formatMessage(message));
  }

  @Override
  public void sendMessage(String message) {
    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      getPlayer().sendMessage(message);
    }
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    getPlayer().sendMessage(BungeeServer.formatMessage(jsonString));
  }

  @Override
  public void sendJSONMessage(String jsonString) {
    getPlayer().sendMessage(ComponentSerializer.parse(jsonString));
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

  @Override
  public String getDisplayName() {
    return getPlayer().getDisplayName();
  }

  @Override
  public String getName() {
    return getPlayer().getName();
  }

  @Override
  public InetAddress getAddress() {
    return getPlayer().getAddress().getAddress();
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
