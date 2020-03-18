package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

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
    sender.sendMessage(BukkitServer.formatMessage(message));
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public boolean isConsole() {
    return !(sender instanceof Player);
  }

  @Override
  public PlayerData getData() {
    if (isConsole()) return plugin.getPlayerStorage().getConsole();

    try {
      return plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(((Player) sender).getUniqueId()));
    } catch (SQLException e) {
      e.printStackTrace();
      sender.sendMessage(Message.get("sender.error.exception").toString());
      return null;
    }
  }
}
