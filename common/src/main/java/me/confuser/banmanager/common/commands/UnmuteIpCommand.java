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
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;

import java.sql.SQLException;
import java.util.List;

public class UnmuteIpCommand extends SingleCommand {

  public UnmuteIpCommand(LocaleManager locale) {
    super(CommandSpec.UNMUTEIP.localize(locale), "unmuteip", CommandPermission.UNMUTEIP, Predicates.alwaysFalse());
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

    final String ipStr = args.get(0);
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message.SENDER_ERROR_INVALID_IP.send(sender, "ip", ipStr);
      return CommandResult.INVALID_ARGS;
    }

    final String reason = args.size() > 1 ? CommandUtils.getReason(1, (String[]) args.toArray()).getMessage() : "";

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final Long ip = CommandUtils.getIp(ipStr);

      if (ip == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", ipStr);
        return;
      }

      if (!plugin.getIpMuteStorage().isMuted(ip)) {
        Message.UNMUTEIP_ERROR_NOEXISTS.send(sender, "ip", ipStr);
        return;
      }

      IpMuteData mute = plugin.getIpMuteStorage().getMute(ip);

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      boolean unmuted;

      try {
        unmuted = plugin.getIpMuteStorage().unmute(mute, actor, reason);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (!unmuted) {
        return;
      }

      String message = Message.UNMUTEIP_NOTIFY.asString(plugin.getLocaleManager(),
              "ip", ipStr,
              "actor", actor.getName(),
              "reason", reason);

      if (!sender.hasPermission("bm.notify.unmuteip")) {
        sender.sendMessage(message);
      }

      CommandUtils.broadcast(message, "bm.notify.unmuteip");
    });

    return CommandResult.SUCCESS;
  }

}
