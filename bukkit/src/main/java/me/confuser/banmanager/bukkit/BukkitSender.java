package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BukkitSender implements CommonSender {

  private BanManagerPlugin plugin;
  private CommandSender sender;

  public BukkitSender(BanManagerPlugin plugin, CommandSender sender) {
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
    sender.sendMessage(message);
  }

  @Override
  public void sendMessage(Message message) {
    sender.sendMessage(message.toString());
  }

  @Override
  public boolean isConsole() {
    return !(sender instanceof Player);
  }

  @Override
  public PlayerData getData() {
    if (isConsole()) return plugin.getPlayerStorage().getConsole();

    return CommonCommand.getPlayer(this, getName(), false);
  }
}
