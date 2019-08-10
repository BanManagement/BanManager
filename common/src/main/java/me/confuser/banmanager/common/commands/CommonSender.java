package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

public interface CommonSender {
  String getName();
  boolean hasPermission(String permission);
  void sendMessage(String message);
  void sendMessage(Message message);
  boolean isConsole();
  PlayerData getData();
}
