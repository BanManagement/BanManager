package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class ReasonsCommand extends AutoCompleteNameTabCommand<BanManager> {

  public ReasonsCommand() {
    super("reasons");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {

    if (args.length != 0) return false;

    for (Map.Entry<String, String> entry : plugin.getReasonsConfig().getReasons().entrySet()) {
      Message.get("reasons.row").set("hashtag", entry.getKey()).set("reason", entry.getValue()).sendTo(sender);
    }

    return true;
  }
}
