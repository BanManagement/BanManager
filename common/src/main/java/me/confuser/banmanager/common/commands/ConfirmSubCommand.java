package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.util.ConfirmationManager;
import me.confuser.banmanager.common.util.Message;

public class ConfirmSubCommand extends CommonSubCommand {

  public ConfirmSubCommand(BanManagerPlugin plugin) {
    super(plugin, "confirm");
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser args) {
    if (sender.isConsole()) {
      sender.sendMessage(Message.getString("sender.error.noConsole"));
      return true;
    }

    CommonPlayer player = (CommonPlayer) sender;
    ConfirmationManager.PendingAction action = ConfirmationManager.getInstance().getAndRemove(player.getUniqueId());

    if (action == null) {
      Message.get("confirmation.expired").sendTo(sender);
      return true;
    }

    Message.get("confirmation.confirmed").sendTo(sender);
    getPlugin().getScheduler().runAsync(action.getAction());

    return true;
  }

  @Override
  public String getHelp() {
    return null;
  }

  @Override
  public String getPermission() {
    return "command.bm.confirm";
  }
}
