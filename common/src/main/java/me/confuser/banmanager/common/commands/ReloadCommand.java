package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.util.Message;

public class ReloadCommand extends CommonCommand {

  public ReloadCommand(BanManagerPlugin plugin) {
    super(plugin, "bmreload", false);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser parser) {
    getPlugin().setupConfigs();

    getPlugin().getServer().callEvent("PluginReloadedEvent", sender.getData());

    Message.get("configReloaded").sendTo(sender);

    return true;
  }
}
