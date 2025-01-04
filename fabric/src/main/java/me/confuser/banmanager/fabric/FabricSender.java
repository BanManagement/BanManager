package me.confuser.banmanager.fabric;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;
import net.minecraft.server.command.ServerCommandSource;
import me.lucko.fabric.api.permissions.v0.Permissions;

import java.sql.SQLException;

public class FabricSender implements CommonSender {

  private BanManagerPlugin plugin;
  private ServerCommandSource sender;

  public FabricSender(BanManagerPlugin plugin, ServerCommandSource sender) {
    this.plugin = plugin;
    this.sender = sender;
  }

  @Override
  public String getName() {
    return sender.getName();
  }

  @Override
  public boolean hasPermission(String permission) {
    return Permissions.check(sender, permission);
  }

  @Override
  public void sendMessage(String message) {
    sender.sendMessage(FabricServer.formatMessage(message));
  }

  @Override
  public void sendMessage(Message message) {
    sendMessage(message.toString());
  }

  @Override
  public boolean isConsole() {
    return !sender.isExecutedByPlayer();
  }

  @Override
  public PlayerData getData() {
    if (isConsole()) return plugin.getPlayerStorage().getConsole();

    try {
      return plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes((sender.getPlayer().getUuid())));
    } catch (SQLException e) {
      e.printStackTrace();
      sendMessage(Message.get("sender.error.exception"));
      return null;
    }
  }
}
