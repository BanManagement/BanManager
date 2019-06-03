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
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.NameBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TempNameBanCommand extends SingleCommand {

  public TempNameBanCommand(LocaleManager locale) {
    super(CommandSpec.TEMPBANNAME.localize(locale), "tempbanname", CommandPermission.TEMPBANNAME, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    CommandParser parser = new CommandParser(argsIn, 2);
    String[] args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission() + ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 3) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, this.getName(), args);
      return CommandResult.SUCCESS;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.NAME_BAN, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    final String name = args[0];
    final long expires = expiresCheck;
    final String reason = parser.getReason().getMessage();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final boolean isBanned = plugin.getNameBanStorage().isBanned(name);

      if (isBanned && !sender.hasPermission("bm.command.tempbanname.override")) {
        Message.BANNAME_ERROR_EXISTS.send(sender, "name", name);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      if (isBanned) {
        NameBanData ban = plugin.getNameBanStorage().getBan(name);

        if (ban != null) {
          try {
            plugin.getNameBanStorage().unban(ban, actor);
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
            e.printStackTrace();
            return;
          }
        }
      }

      final NameBanData ban = new NameBanData(name, actor, reason, expires);
      boolean created;

      try {
        created = plugin.getNameBanStorage().ban(ban, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.BANNAME_ERROR_EXISTS.asString(plugin.getLocaleManager(), "player", name));
        return;
      }

      if (!created) {
        return;
      }

      // Find online players
      plugin.getBootstrap().getScheduler().executeSync(() -> {
        String kickMessage = Message.TEMPBANNAME_NAME_KICK.asString(plugin.getLocaleManager(),
                                     "reason", ban.getReason(),
                                     "name", name,
                                     "actor", actor.getName());

        for (UUID onlineUUID : plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList())) {
          plugin.getBootstrap().getPlayerAsSender(onlineUUID).ifPresent(target -> {
            if(target.getName().equalsIgnoreCase(name)) {
              target.kick(kickMessage);
            }
          });
        }

      });

    });

    return CommandResult.SUCCESS;
  }

}
