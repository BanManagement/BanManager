package me.confuser.banmanager.common;

import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

import java.util.UUID;

public class TestSender implements CommonSender {

  private final UUID uuid;
  private final String name;
  private final boolean onlineMode;

  public TestSender(UUID uuid, String name, boolean onlineMode) {
    this.uuid = uuid;
    this.name = name;
    this.onlineMode = onlineMode;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean hasPermission(String permission) {
    return true;
  }

  @Override
  public void sendMessage(String message) {

  }

  @Override
  public void sendMessage(Message message) {

  }

  @Override
  public boolean isConsole() {
    return true;
  }

  @Override
  public PlayerData getData() {
    if (isConsole()) return BanManagerPlugin.getInstance().getPlayerStorage().getConsole();

    return CommonCommand.getPlayer(this, getName(), false);
  }
}
