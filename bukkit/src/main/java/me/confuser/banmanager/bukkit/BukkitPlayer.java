package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

  public BukkitPlayer(Player player, boolean onlineMode) {
    this(player.getUniqueId(), player.getName(), onlineMode);
  }

  public void kick(String message) {
    getPlayer().kickPlayer(message);
  }

  public void sendMessage(String message) {
    getPlayer().sendMessage(message);
  }

  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  public boolean isConsole() {
    return false;
  }

  public PlayerData getData() {
    return null;
  }

  public boolean isOnlineMode() {
    return onlineMode;
  }

  public boolean hasPermission(String permission) {
    return getPlayer().hasPermission(permission);
  }

  public String getDisplayName() {
    return getPlayer().getDisplayName();
  }

  public String getName() {
    return getPlayer().getName();
  }

  public InetAddress getAddress() {
    return getPlayer().getAddress().getAddress();
  }

  public UUID getUniqueId() {
    return getPlayer().getUniqueId();
  }

  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    Player player = getPlayer();
    Location location = new Location(Bukkit.getWorld(world.getName()), x, y, z, yaw, pitch);

    if (player.isInsideVehicle()) player.leaveVehicle();

    return player.teleport(location);
  }

  public boolean isOnline() {
    return getPlayer() != null;
  }

  private Player getPlayer() {
    if (isOnlineMode()) return Bukkit.getServer().getPlayer(uuid);

    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
      if (UUIDUtils.createOfflineUUID(onlinePlayer.getName()).equals(uuid)) return onlinePlayer;
    }

    return null;
  }
}
