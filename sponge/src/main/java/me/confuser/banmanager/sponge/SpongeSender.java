package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

public class SpongeSender implements CommonSender {

  private BanManagerPlugin plugin;
  private CommandSource sender;

  public SpongeSender(BanManagerPlugin plugin, CommandSource sender) {
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
    sender.sendMessage(Text.of(message));
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

    return CommonCommand.getPlayer(this, getName(), false);
  }
}
