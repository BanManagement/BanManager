package me.confuser.banmanager.common;

import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import java.util.UUID;

public class TestSender implements CommonSender {

  private final BanManagerPlugin plugin;
  private final UUID uuid;
  private final String name;
  private final boolean onlineMode;

  public TestSender(BanManagerPlugin plugin, UUID uuid, String name, boolean onlineMode) {
    this.plugin = plugin;
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
  public boolean isConsole() {
    return true;
  }

  @Override
  public PlayerData getData() {
    if (isConsole()) return plugin.getPlayerStorage().getConsole();

    return CommonCommand.resolvePlayer(plugin, this, getName(), false);
  }
}
