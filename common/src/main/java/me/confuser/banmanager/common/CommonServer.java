package me.confuser.banmanager.common;

import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.MessageRenderer;

import java.util.UUID;

public interface CommonServer {
  CommonPlayer getPlayer(UUID uniqueId);

  CommonPlayer getPlayer(String name);

  CommonPlayer getPlayerExact(String name);

  CommonPlayer[] getOnlinePlayers();

  void broadcast(String message, String permission);

  default void broadcast(Message message, String permission) {
    for (CommonPlayer player : getOnlinePlayers()) {
      if (player.hasPermission(permission)) {
        player.sendMessage(message.resolveComponentFor(player));
      }
    }
    getConsoleSender().sendMessage(MessageRenderer.getInstance().toPlainText(message.resolveComponent()));
  }

  // Console receives plain text since it cannot render Components with hover/click events
  default void broadcast(Component message, String permission) {
    for (CommonPlayer player : getOnlinePlayers()) {
      if (player.hasPermission(permission)) {
        player.sendMessage(message);
      }
    }
    getConsoleSender().sendMessage(MessageRenderer.getInstance().toPlainText(message));
  }

  void broadcast(String message, String permission, CommonSender sender);

  CommonSender getConsoleSender();

  boolean dispatchCommand(CommonSender consoleSender, String command);

  CommonWorld getWorld(String name);

  CommonEvent callEvent(String name, Object... args);

  CommonExternalCommand getPluginCommand(String commandName);
}
