package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.Message;

import java.util.Map;

public class ReasonsCommand extends CommonCommand {

  public ReasonsCommand(BanManagerPlugin plugin) {
    super(plugin, "reasons");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length != 0) return false;

    for (Map.Entry<String, String> entry : getPlugin().getReasonsConfig().getReasons().entrySet()) {
      Message.get("reasons.row").set("hashtag", entry.getKey()).set("reason", entry.getValue()).sendTo(sender);
    }

    return true;
  }
}
