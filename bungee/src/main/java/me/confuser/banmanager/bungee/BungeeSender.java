package me.confuser.banmanager.bungee;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeSender implements CommonSender {

  private BanManagerPlugin plugin;
  private CommandSender sender;

  public BungeeSender(BanManagerPlugin plugin, CommandSender sender) {
    this.plugin = plugin;
    this.sender = sender;
  }

  @Override
  public String getName() {
    return sender.getName();
  }

  @Override
  public boolean hasPermission(String permission) {
    return sender.hasPermission(permission);
  }

  @Override
  public void sendMessage(String message) {
    sender.sendMessage(BungeeServer.formatMessage(message));
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public boolean isConsole() {
    return !(sender instanceof ProxiedPlayer);
  }

  @Override
  public PlayerData getData() {
    if (isConsole()) return plugin.getPlayerStorage().getConsole();

    return CommonCommand.getPlayer(this, getName(), false);
  }
}
