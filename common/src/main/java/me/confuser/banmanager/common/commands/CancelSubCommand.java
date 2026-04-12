package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.util.ConfirmationManager;
import me.confuser.banmanager.common.util.Message;

public class CancelSubCommand extends CommonSubCommand {

  public CancelSubCommand(BanManagerPlugin plugin) {
    super(plugin, "cancel");
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser args) {
    if (sender.isConsole()) {
      sender.sendMessage(Message.getString("sender.error.noConsole"));
      return true;
    }

    CommonPlayer player = (CommonPlayer) sender;

    if (ConfirmationManager.getInstance().cancel(player.getUniqueId())) {
      Message.get("confirmation.cancelled").sendTo(sender);
    } else {
      Message.get("confirmation.expired").sendTo(sender);
    }

    return true;
  }

  @Override
  public String getHelp() {
    return null;
  }

  @Override
  public String getPermission() {
    return "command.bm.cancel";
  }
}
