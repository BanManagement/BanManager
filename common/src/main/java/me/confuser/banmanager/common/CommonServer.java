package me.confuser.banmanager.common;

import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import net.kyori.text.TextComponent;

import java.util.UUID;

public interface CommonServer {
  CommonPlayer getPlayer(UUID uniqueId);

  CommonPlayer getPlayer(String name);

  CommonPlayer[] getOnlinePlayers();

  void broadcast(String message, String permission);

  void broadcastJSON(TextComponent message, String permission);

  void broadcast(String message, String permission, CommonSender sender);

  CommonSender getConsoleSender();

  boolean dispatchCommand(CommonSender consoleSender, String command);

  CommonWorld getWorld(String name);

  CommonEvent callEvent(String name, Object... args);

  CommonExternalCommand getPluginCommand(String commandName);
}
