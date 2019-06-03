package me.confuser.banmanager.common.commands.global;

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
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.global.GlobalIpBanData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class TempBanIpAllCommand extends SingleCommand {

  public TempBanIpAllCommand(LocaleManager locale) {
    super(CommandSpec.TEMPBANIPALL.localize(locale), "tempbanipall", CommandPermission.TEMPBANIPALL, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 3) {
      return CommandResult.INVALID_ARGS;
    }

    final String ipStr = args.get(0);
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message.SENDER_ERROR_INVALID_IP.send(sender, "ip", ipStr);
      return CommandResult.INVALID_ARGS;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args.get(1), true);
    } catch (Exception e1) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getTimeLimits().isPastLimit(sender, TimeLimitType.IP_BAN, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    final long expires = expiresCheck;

    final String reason = StringUtils.join(args, " ", 2, args.size());

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final long ip;

      if (isName) {
        PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
        if (player == null) {
          Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", ipStr);
          return;
        }

        ip = player.getIp();
      } else {
        ip = IPUtils.toLong(ipStr);
      }

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

      final GlobalIpBanData ban = new GlobalIpBanData(ip, actor, reason, expires);
      int created;

      try {
        created = plugin.getGlobalIpBanStorage().create(ban);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (created != 1) {
        return;
      }

      Message.TEMPBANALL_NOTIFY.send(sender,
             "ip", ipStr,
             "actor", ban.getActorName(),
             "reason", ban.getReason(),
             "expires", DateUtils.getDifferenceFormat(ban.getExpires()));
    });

    return CommandResult.SUCCESS;
  }

}
