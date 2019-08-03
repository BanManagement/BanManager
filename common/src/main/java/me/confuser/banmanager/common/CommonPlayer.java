package me.confuser.banmanager.common;

import me.confuser.banmanager.common.util.Message;

public interface CommonPlayer {
  void kick(String message);
  void sendMessage(String message);
  void sendMessage(Message message);
  boolean isOnlineMode();
}
