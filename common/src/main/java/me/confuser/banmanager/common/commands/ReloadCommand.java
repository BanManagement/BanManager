package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;

import java.util.List;

public class ReloadCommand extends SingleCommand {

  public ReloadCommand(LocaleManager locale) {
    super(CommandSpec.BMRELOAD.localize(locale), "bmreload", CommandPermission.BMRELOAD, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    plugin.getConfiguration().load();
    //plugin.getExemptionsConfig().load();
    //plugin.getReasonsConfig().load();
    //plugin.getGeoIpConfig().load();

    Message.CONFIGRELOADED.send(sender);

    return CommandResult.SUCCESS;
  }
}
