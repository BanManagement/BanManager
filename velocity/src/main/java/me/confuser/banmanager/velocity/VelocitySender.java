package me.confuser.banmanager.velocity;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

public class VelocitySender implements CommonSender {

  private BanManagerPlugin plugin;
  private CommandSource commandSource;

  public VelocitySender(BanManagerPlugin plugin, CommandSource commandSource) {
    this.plugin = plugin;
    this.commandSource = commandSource;
  }

  @Override
  public String getName() {
    return isConsole() ? "CONSOLE" : ((Player) commandSource).getUsername();
  }

  @Override
  public boolean hasPermission(String permission) {return commandSource.hasPermission(permission); }

  @Override
  public void sendMessage(String message) {
    commandSource.sendMessage(VelocityServer.formatMessage(message));
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public boolean isConsole() {
    return !(commandSource instanceof Player);
  }

  @Override
  public PlayerData getData() {
    if (isConsole()) return plugin.getPlayerStorage().getConsole();

    return CommonCommand.getPlayer(this, getName(), false);
  }
}
