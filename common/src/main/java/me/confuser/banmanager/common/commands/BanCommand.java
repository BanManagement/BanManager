package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.util.Message;

public class BanCommand extends CommonCommand {

  public BanCommand() {
    super("ban");
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser args) {
    final boolean isSilent = args.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }


  }
}
