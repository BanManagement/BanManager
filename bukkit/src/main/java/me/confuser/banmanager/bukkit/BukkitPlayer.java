package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;
import me.confuser.banmanager.common.util.MessageRegistry;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.UUID;

public class BukkitPlayer implements CommonPlayer {
  private static final boolean PAPER_ADVENTURE;

  static {
    boolean paper = false;
    try {
      Class.forName("io.papermc.paper.adventure.PaperAdventure");
      paper = true;
    } catch (ClassNotFoundException ignored) {
    }
    PAPER_ADVENTURE = paper;
  }

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

  @Override
  public void kick(Component component) {
    if (PAPER_ADVENTURE) {
      PaperAdventureHelper.kick(getPlayer(), component);
    } else {
      kick(MessageRenderer.getInstance().toLegacy(component));
    }
  }

  public void sendMessage(String message) {
    if(message.isEmpty()) return;

    if(Message.isJSONMessage(message)) {
      sendJSONMessage(message);
    } else {
      getPlayer().sendMessage(BukkitServer.formatMessage(message));
    }
  }

  @Override
  public void sendMessage(Component component) {
    if (PAPER_ADVENTURE) {
      PaperAdventureHelper.sendMessage(getPlayer(), component);
    } else {
      String json = MessageRenderer.getInstance().toJson(component);
      getPlayer().spigot().sendMessage(ComponentSerializer.parse(json));
    }
  }

  @Override
  public void sendActionBar(Component component) {
    if (PAPER_ADVENTURE) {
      PaperAdventureHelper.sendActionBar(getPlayer(), component);
    } else {
      String json = MessageRenderer.getInstance().toJson(component);
      getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, ComponentSerializer.parse(json));
    }
  }

  @Override
  public void showTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
    if (PAPER_ADVENTURE) {
      PaperAdventureHelper.showTitle(getPlayer(), title, subtitle, fadeIn, stay, fadeOut);
    } else {
      MessageRenderer renderer = MessageRenderer.getInstance();
      String legacyTitle = title != null ? renderer.toLegacy(title) : "";
      String legacySubtitle = subtitle != null ? renderer.toLegacy(subtitle) : "";
      getPlayer().sendTitle(legacyTitle, legacySubtitle, fadeIn, stay, fadeOut);
    }
  }

  @Override
  public void playSound(String sound, float volume, float pitch) {
    Player p = getPlayer();
    if (p == null) return;
    try {
      p.playSound(p.getLocation(), sound, volume, pitch);
    } catch (IllegalArgumentException ignored) {
      // Invalid sound key -- silently ignore
    }
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    getPlayer().spigot().sendMessage(ComponentSerializer.parse(GsonComponentSerializer.gson().serialize(jsonString)));
  }

  @Override
  public void sendJSONMessage(String message) {
    getPlayer().spigot().sendMessage(ComponentSerializer.parse(message));
  }

  public boolean isConsole() {
    return false;
  }

  public PlayerData getData() {
    try {
      return BanManagerPlugin.getInstance().getPlayerStorage().queryForId(UUIDUtils.toBytes(getUniqueId()));
    } catch (SQLException e) {
      BanManagerPlugin.getInstance().getLogger().warning("Failed to load player data", e);
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

  @Override
  public String getLocale() {
    Player p = getPlayer();
    if (p == null) return "en";
    return MessageRegistry.normaliseLocale(p.getLocale());
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
