package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.util.UUID;

public class BukkitPlayer implements CommonPlayer {

  private final UUID uuid;
  private final String name;
  private final boolean onlineMode;

  public BukkitPlayer(UUID uuid, String name, boolean onlineMode) {
    this.uuid = uuid;
    this.name = name;
    this.onlineMode = onlineMode;
  }

  @Override
  public void kick(String message) {
    getPlayer().kickPlayer(message);
  }

  @Override
  public void sendMessage(String message) {
    getPlayer().sendMessage(message);
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public boolean isOnlineMode() {
    return onlineMode;
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

  private Player getPlayer() {
    if (isOnlineMode()) return Bukkit.getServer().getPlayer(uuid);

    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
      if (UUIDUtils.createOfflineUUID(onlinePlayer.getName()).equals(uuid)) return onlinePlayer;
    }

    return null;
  }
}
