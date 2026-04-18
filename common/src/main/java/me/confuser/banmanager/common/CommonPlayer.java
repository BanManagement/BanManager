package me.confuser.banmanager.common;

import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;

import java.net.InetAddress;
import java.util.UUID;

public interface CommonPlayer extends CommonSender {

  void kick(String message);

  default void kick(Message message) {
    kick(message.resolveComponentFor(this));
  }

  default void kick(Component component) {
    kick(MessageRenderer.getInstance().toLegacy(component));
  }

  void sendMessage(String message);

  @Override
  default void sendMessage(Message message) {
    sendMessage(message.resolveComponentFor(this));
  }

  @Override
  default void sendMessage(Component component) {
    sendMessage(MessageRenderer.getInstance().toLegacy(component));
  }

  default void sendActionBar(Component component) {
    sendMessage(component);
  }

  default void showTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
    if (title != null) sendMessage(title);
    if (subtitle != null) sendMessage(subtitle);
  }

  default void playSound(String sound, float volume, float pitch) { }

  void sendJSONMessage(TextComponent jsonString);

  void sendJSONMessage(String jsonString);

  @Override
  boolean isConsole();

  @Override
  PlayerData getData();

  boolean isOnlineMode();

  boolean isOnline();

  boolean hasPermission(String permission);

  String getDisplayName();

  String getName();

  InetAddress getAddress();

  UUID getUniqueId();

  boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw);

  boolean canSee(CommonPlayer player);

  String getLocale();
}
