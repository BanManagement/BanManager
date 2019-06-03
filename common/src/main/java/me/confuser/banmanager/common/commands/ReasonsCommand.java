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
import java.util.Map;

public class ReasonsCommand extends SingleCommand {

  public ReasonsCommand(LocaleManager locale) {
    super(CommandSpec.REASONS.localize(locale), "reasons", CommandPermission.REASONS, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() != 0) return CommandResult.INVALID_ARGS;

    for (Map.Entry<String, String> entry : plugin.getReasonsConfig().getReasons().entrySet()) {
      Message.REASONS_ROW.send(sender, "hashtag", entry.getKey(), "reason", entry.getValue());
    }

    return CommandResult.SUCCESS;
  }
}
