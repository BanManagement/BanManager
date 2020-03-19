package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

public class BukkitPlayer implements CommonPlayer {
  private Player player;
  private final UUID uuid;
  private InetAddress address;
  private final boolean onlineMode;

  public BukkitPlayer(UUID uuid, String name, boolean onlineMode) {
    this.uuid = uuid;
    this.onlineMode = onlineMode;
  }

  public BukkitPlayer(Player player, boolean onlineMode) {
    this(player.getUniqueId(), player.getName(), onlineMode);

    this.player = player;
  }

  public BukkitPlayer(Player player, boolean onlineMode, InetAddress address) {
    this(player, onlineMode);

    this.address = address;
  }

  public void kick(String message) {
    getPlayer().kickPlayer(BukkitServer.formatMessage(message));
  }

  public void sendMessage(String message) {
    getPlayer().sendMessage(BukkitServer.formatMessage(message));
  }

  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    getPlayer().spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.INSTANCE.serialize(jsonString)));
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
    return getPlayer().hasPermission(permission);
  }

  public String getDisplayName() {
    return getPlayer().getDisplayName();
  }

  public String getName() {
    return getPlayer().getName();
  }

  public InetAddress getAddress() {
    if (address != null) return address;
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

  @Override
  public boolean canSee(CommonPlayer player) {
    return getPlayer().canSee(Bukkit.getPlayer(player.getUniqueId()));
  }

  public boolean isOnline() {
    return getPlayer() != null;
  }

  private Player getPlayer() {
    if (player != null) return player;
    if (isOnlineMode()) return Bukkit.getServer().getPlayer(uuid);

    for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
      if (UUIDUtils.createOfflineUUID(onlinePlayer.getName()).equals(uuid)) return onlinePlayer;
    }

    return null;
  }
}
