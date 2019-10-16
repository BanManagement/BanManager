package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.CommonServer;
import me.confuser.banmanager.common.CommonWorld;
import me.confuser.banmanager.common.commands.CommonSender;
import org.spongepowered.api.Sponge;

import java.util.UUID;

public class SpongeServer implements CommonServer {

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    return null;
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    return null;
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return new CommonPlayer[0];
  }

  @Override
  public void broadcast(String message, String permission) {
  }

  public void broadcast(String message, String permission, CommonSender sender) {

  }

  public CommonSender getConsoleSender() {
    return null;
  }

  public boolean dispatchCommand(CommonSender consoleSender, String command) {
    return false;
  }

  public CommonWorld getWorld(String name) {
    return null;
  }
}
