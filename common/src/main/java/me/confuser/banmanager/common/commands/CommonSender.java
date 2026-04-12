package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;

public interface CommonSender {
  String getName();
  boolean hasPermission(String permission);
  void sendMessage(String message);
  boolean isConsole();
  PlayerData getData();

  default void sendMessage(Message message) {
    sendMessage(message.resolveComponent());
  }

  default void sendMessage(Component component) {
    sendMessage(MessageRenderer.getInstance().toLegacy(component));
  }
}
