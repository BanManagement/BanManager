package me.confuser.banmanager.common;

import java.util.UUID;

public interface CommonServer {
  CommonPlayer getPlayer(UUID uniqueId);
  CommonPlayer getPlayer(String name);

  CommonPlayer[] getOnlinePlayers();
}
