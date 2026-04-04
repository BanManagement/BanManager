package me.confuser.banmanager.common;

import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.util.Message;

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
        player.sendMessage(message.resolveFor(player));
      }
    }
    getConsoleSender().sendMessage(message.toString());
  }

  void broadcastJSON(TextComponent message, String permission);

  void broadcast(String message, String permission, CommonSender sender);

  CommonSender getConsoleSender();

  boolean dispatchCommand(CommonSender consoleSender, String command);

  CommonWorld getWorld(String name);

  CommonEvent callEvent(String name, Object... args);

  CommonExternalCommand getPluginCommand(String commandName);
}
