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
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.*;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TempIpRangeBanCommand extends SingleCommand {

  public TempIpRangeBanCommand(LocaleManager locale) {
    super(CommandSpec.TEMPBANIPRANGE.localize(locale), "tempbaniprange", CommandPermission.TEMPBANIPRANGE, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    CommandParser parser = new CommandParser(argsIn, 2);
    String[] args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission()+ ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 3) {
      return CommandResult.INVALID_ARGS;
    }

    String ipStr = args[0];
    long[] range = null;

    if (ipStr.contains("*")) {
      // Simple wildcard logic
      range = IPUtils.getRangeFromWildcard(ipStr);
    } else if (ipStr.contains("/")) {
      // cidr notation
      range = IPUtils.getRangeFromCidrNotation(ipStr);
    }

    if (range == null) {
      Message.BANIPRANGE_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    final long fromIp = range[0];
    final long toIp = range[1];

    if (fromIp > toIp) {
      Message.BANIPRANGE_ERROR_MINMAX.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getIpRangeBanStorage().isBanned(fromIp) || plugin.getIpRangeBanStorage().isBanned(toIp)) {
      Message.BANIPRANGE_ERROR_EXISTS.send(sender);
      return CommandResult.SUCCESS;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getTimeLimits().isPastLimit(sender, TimeLimitType.IP_BAN, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    final long expires = expiresCheck;

    final String reason = parser.getReason().getMessage();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData actor;

      if (!sender.isConsole()) {
        try {
          actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(sender));
        } catch (SQLException e) {
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }
      } else {
        actor = plugin.getPlayerStorage().getConsole();
      }

      final IpRangeBanData ban = new IpRangeBanData(fromIp, toIp, actor, reason, expires);
      boolean created;

      try {
        created = plugin.getIpRangeBanStorage().ban(ban, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.BANIPRANGE_ERROR_EXISTS.getMessage());
        return;
      }

      if (!created) {
        return;
      }

      // Find online players
      plugin.getBootstrap().getScheduler().executeSync(() -> {
        String kickMessage = Message.TEMPBANIPRANGE_IP_KICK.asString(plugin.getLocaleManager(),
                                     "reason", ban.getReason(),
                                     "actor", actor.getName(),
                                     "expires", DateUtils.getDifferenceFormat(ban.getExpires()));

        for (UUID onlineUUID : plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList())) {
          plugin.getBootstrap().getPlayerAsSender(onlineUUID).ifPresent(target -> {
            if(ban.inRange(IPUtils.toLong(target.getIPAddress()))) {
              target.kick(kickMessage);
            }
          });
        }

      });
    });

    return CommandResult.SUCCESS;
  }

}
