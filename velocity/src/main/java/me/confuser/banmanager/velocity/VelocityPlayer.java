package me.confuser.banmanager.velocity;

import com.velocitypowered.api.proxy.Player;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;
import me.confuser.banmanager.common.util.MessageRegistry;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;

import java.net.InetAddress;
import java.time.Duration;
import java.util.UUID;

public class VelocityPlayer implements CommonPlayer {
  // Only used for legacy sendJSONMessage(String) path; Component methods use full-color gson
  private static final GsonComponentSerializer DOWNSAMPLING_GSON =
      GsonComponentSerializer.builder().downsampleColors().build();

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
  public void kick(Component component) {
    player.disconnect(convertToNative(component));
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
  public void sendMessage(Component component) {
    player.sendMessage(convertToNative(component));
  }

  @Override
  public void sendActionBar(Component component) {
    player.sendActionBar(convertToNative(component));
  }

  @Override
  public void showTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
    net.kyori.adventure.text.Component nativeTitle = title != null
        ? convertToNative(title) : net.kyori.adventure.text.Component.empty();
    net.kyori.adventure.text.Component nativeSubtitle = subtitle != null
        ? convertToNative(subtitle) : net.kyori.adventure.text.Component.empty();

    Title.Times times = Title.Times.of(
        Duration.ofMillis(fadeIn * 50L),
        Duration.ofMillis(stay * 50L),
        Duration.ofMillis(fadeOut * 50L)
    );
    player.showTitle(Title.title(nativeTitle, nativeSubtitle, times));
  }

  @Override
  public void playSound(String sound, float volume, float pitch) {
    try {
      player.playSound(Sound.sound(Key.key(sound), Sound.Source.MASTER, volume, pitch));
    } catch (IllegalArgumentException ignored) {
      // Invalid sound key -- silently ignore
    }
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
    player.sendMessage(VelocityServer.convert(jsonString));
  }

  @Override
  public void sendJSONMessage(String jsonString) {
    player.sendMessage(DOWNSAMPLING_GSON.deserialize(jsonString));
  }

  private net.kyori.adventure.text.Component convertToNative(Component component) {
    String json = MessageRenderer.getInstance().toJson(component);
    return GsonComponentSerializer.gson().deserialize(json);
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
    return this.uuid;
  }

  @Override
  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }

  @Override
  public boolean canSee(CommonPlayer player) {
    return true;
  }

  @Override
  public String getLocale() {
    java.util.Locale locale = player.getEffectiveLocale();
    if (locale == null) return "en";
    return MessageRegistry.normaliseLocale(locale.toString());
  }
}
