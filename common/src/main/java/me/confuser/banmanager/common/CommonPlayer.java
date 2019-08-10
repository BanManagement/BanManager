package me.confuser.banmanager.common;

import me.confuser.banmanager.common.util.Message;

import java.net.InetAddress;

public interface CommonPlayer {
  void kick(String message);
  void sendMessage(String message);
  void sendMessage(Message message);
  boolean isOnlineMode();
  boolean hasPermission(String permission);
  String getDisplayName();
  String getName();
  InetAddress getAddress();
}
