package me.confuser.banmanager.common.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.IpRangeBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;

public class UnbanIpRangeCommand extends SingleCommand {

  public UnbanIpRangeCommand(LocaleManager locale) {
    super(CommandSpec.UNBANIPRANGE.localize(locale), "unbaniprange", CommandPermission.UNBANIPRANGE, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 1) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, this.getName(), args);
      return CommandResult.SUCCESS;
    }

    final String ipStr = args.get(0);
    long[] range = new long[2];
    final boolean isName;

    if (ipStr.contains("*")) {
      // Simple wildcard logic
      range = IPUtils.getRangeFromWildcard(ipStr);
      isName = false;
    } else if (ipStr.contains("/")) {
      // cidr notation
      range = IPUtils.getRangeFromCidrNotation(ipStr);
      isName = false;
    } else if (InetAddresses.isInetAddress(ipStr)) {
      range[0] = IPUtils.toLong(ipStr);
      range[1] = range[0];
      isName = false;
    } else if (ipStr.length() <= 16) {
      isName = true;
    } else {
      Message.SENDER_ERROR_INVALID_IP.send(sender, "ip", ipStr);
      return CommandResult.INVALID_ARGS;
    }

    if (!isName && range == null) {
      Message.BANIPRANGE_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    final long[] ranges = range;
    final String reason = args.size() > 1 ? CommandUtils.getReason(1, args).getMessage() : "";

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      long[] range1 = new long[2];

      if (isName) {
        PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
        if (player == null) {
          Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", ipStr);
          return;
        }

        range1[0] = player.getIp();
        range1[1] = player.getIp();
      } else {
        range1 = ranges;
      }

      if (!plugin.getIpRangeBanStorage().isBanned(range1[0]) && !plugin.getIpRangeBanStorage().isBanned(range1[1])) {
        Message.UNBANIP_ERROR_NOEXISTS.send(sender, "ip", ipStr);
        return;
      }

      IpRangeBanData ban = plugin.getIpRangeBanStorage().getBan(range1[0]);

      if (ban == null) ban = plugin.getIpRangeBanStorage().getBan(range1[1]);

      PlayerData actor;

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

      boolean unbanned;

      try {
        unbanned = plugin.getIpRangeBanStorage().unban(ban, actor, reason);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (!unbanned) {
        return;
      }

      String message = Message.UNBANIPRANGE_NOTIFY.asString(plugin.getLocaleManager(),
                    "from", IPUtils.toString(ban.getFromIp()),
              "to", IPUtils.toString(ban.getToIp()),
              "actor", actor.getName(),
              "reason", reason);

      if (!sender.hasPermission("bm.notify.unbaniprange")) {
        sender.sendMessage(message);
      }

      CommandUtils.broadcast(message.toString(), "bm.notify.unbaniprange");
    });

    return CommandResult.SUCCESS;
  }
}
