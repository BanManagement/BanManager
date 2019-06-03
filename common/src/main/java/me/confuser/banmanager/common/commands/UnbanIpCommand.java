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
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;

public class UnbanIpCommand extends SingleCommand {

  public UnbanIpCommand(LocaleManager locale) {
    super(CommandSpec.UNBANIP.localize(locale), "unbanip", CommandPermission.UNBANIP, Predicates.alwaysFalse());
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
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message.SENDER_ERROR_INVALID_IP.send(sender, "ip", ipStr);
      return CommandResult.INVALID_ARGS;
    }

    final String reason = args.size() > 1 ? CommandUtils.getReason(1, args).getMessage() : "";

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

      if (!plugin.getIpBanStorage().isBanned(ip)) {
        Message.UNBANIP_ERROR_NOEXISTS.send(sender, "ip", ipStr);
        return;
      }

      IpBanData ban = plugin.getIpBanStorage().getBan(ip);

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
        unbanned = plugin.getIpBanStorage().unban(ban, actor, reason);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (!unbanned) {
        return;
      }

      String message = Message.UNBANIP_NOTIFY.asString(plugin.getLocaleManager(),
              "ip", ipStr,
              "actor", actor.getName(),
              "reason", reason);

      if (!sender.hasPermission("bm.notify.unbanip")) {
        sender.sendMessage(message);
      }

      CommandUtils.broadcast(message, "bm.notify.unbanip");
    });

    return CommandResult.SUCCESS;
  }
}
