package me.confuser.banmanager.common.listeners;

import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.data.PlayerData;

public interface CommonJoinHandler {
  void handleDeny(Message message);
  void handlePlayerDeny(PlayerData player, Message message);
}
