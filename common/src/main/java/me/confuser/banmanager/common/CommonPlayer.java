package me.confuser.banmanager.common;

import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import net.kyori.text.TextComponent;

import java.net.InetAddress;
import java.util.UUID;

public interface CommonPlayer extends CommonSender {

  void kick(String message);

  void sendMessage(String message);

  void sendMessage(Message message);

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
}
