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
import me.confuser.banmanager.data.IpBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.global.GlobalIpBanRecordData;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;

public class UnbanIpAllCommand extends SingleCommand {

  public UnbanIpAllCommand(LocaleManager locale) {
    super(CommandSpec.UNBANIPALL.localize(locale), "unbanipall", CommandPermission.UNBANIPALL, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 1) {
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String ipStr = args.get(0);
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message.SENDER_ERROR_INVALID_IP.send(sender, "ip", ipStr);
      return CommandResult.INVALID_ARGS;
    }

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

      IpBanData ban = plugin.getIpBanStorage().getBan(ip);

      if (ban == null) {
        Message.UNBANIP_ERROR_NOEXISTS.send(sender, "ip", ipStr);
        return;
      }

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

      GlobalIpBanRecordData record = new GlobalIpBanRecordData(ban.getIp(), actor);

      int unbanned;

      try {
        unbanned = plugin.getGlobalIpBanRecordStorage().create(record);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (unbanned == 0) {
        return;
      }

      Message.UNBANIPALL_NOTIFY.send(sender,
              "actor", actor.getName(),
              "ip", ban.getIp());
    });

    return CommandResult.SUCCESS;
  }

}
