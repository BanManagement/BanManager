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
import me.confuser.banmanager.data.NameBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;

import java.sql.SQLException;
import java.util.List;

public class UnbanNameCommand extends SingleCommand {

  public UnbanNameCommand(LocaleManager locale) {
    super(CommandSpec.UNBANNAME.localize(locale), "unbanname", CommandPermission.UNBANNAME, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 1) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, this.getName(), (String[]) args.toArray());
      return CommandResult.SUCCESS;
    }

    final String name = args.get(0);
    final String reason = args.size() > 1 ? CommandUtils.getReason(1, (String[]) args.toArray()).getMessage() : "";

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      if (!plugin.getNameBanStorage().isBanned(name)) {
        Message.UNBANNAME_ERROR_NOEXISTS.send(sender, "name", name);
        return;
      }

      NameBanData ban = plugin.getNameBanStorage().getBan(name);
      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      boolean unbanned;

      try {
        unbanned = plugin.getNameBanStorage().unban(ban, actor, reason);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (!unbanned) {
        return;
      }

      String message = Message.UNBANNAME_NOTIFY.asString(plugin.getLocaleManager(),
              "name", name,
              "actor", actor.getName(),
              "reason", reason);

      if (!sender.hasPermission("bm.notify.unbanname")) {
        sender.sendMessage(message);
      }

      CommandUtils.broadcast(message, "bm.notify.unbanname");
    });

    return CommandResult.SUCCESS;
  }

}
