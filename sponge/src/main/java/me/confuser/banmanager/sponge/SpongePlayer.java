package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

public class SpongePlayer implements CommonPlayer {

  private final UUID uuid;
  private final String name;
  private final boolean onlineMode;

  public SpongePlayer(UUID uuid, String name, boolean onlineMode) {
    this.uuid = uuid;
    this.name = name;
    this.onlineMode = onlineMode;
  }

  public SpongePlayer(Player player, boolean onlineMode) {
    this(player.getUniqueId(), player.getName(), onlineMode);
  }

  @Override
  public void kick(String message) {
    getPlayer().kick(TextSerializers.FORMATTING_CODE.deserialize(message));
  }

  @Override
  public void sendMessage(String message) {
    getPlayer().sendMessage(TextSerializers.FORMATTING_CODE.deserialize(message));
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public boolean isConsole() {
    return false;
  }

  @Override
  public PlayerData getData() {
    return null;
  }

  @Override
  public boolean isOnlineMode() {
    return onlineMode;
  }

  public boolean isOnline() {
    return getPlayer() != null;
  }

  @Override
  public boolean hasPermission(String permission) {
    return getPlayer().hasPermission(permission);
  }

  @Override
  public String getDisplayName() {
    return getPlayer().getDisplayNameData().displayName().get().toPlain();
  }

  @Override
  public String getName() {
    return getPlayer().getName();
  }

  @Override
  public InetAddress getAddress() {
    return getPlayer().getConnection().getAddress().getAddress();
  }

  public UUID getUniqueId() {
    return uuid;
  }

  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }

  private Player getPlayer() {
    if (isOnlineMode()) {
      Optional<Player> player = Sponge.getServer().getPlayer(uuid);

      if (player.isPresent()) {
        return player.get();
      } else {
        return null;
      }
    }

    for (Player onlinePlayer : Sponge.getServer().getOnlinePlayers()) {
      if (UUIDUtils.createOfflineUUID(onlinePlayer.getName()).equals(uuid)) return onlinePlayer;
    }

    return null;
  }
}
