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
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.IpMuteData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TempIpMuteCommand extends SingleCommand {

  public TempIpMuteCommand(LocaleManager locale) {
    super(CommandSpec.TEMPMUTEIP.localize(locale), "tempmuteip", CommandPermission.TEMPMUTEIP, Predicates.alwaysFalse());
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

    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(this.getPermission().get().getPermission() + ".soft")) {
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

    final String ipStr = args[0];
    final boolean isName = !InetAddresses.isInetAddress(ipStr);

    if (isName && ipStr.length() > 16) {
      Message.SENDER_ERROR_INVALID_IP.send(sender, "ip", ipStr);
      return CommandResult.INVALID_ARGS;
    }

    if (isName) {
      Sender onlinePlayer = plugin.getBootstrap().getPlayerAsSender(ipStr).orElse(null);

      if (onlinePlayer != null && !sender.hasPermission("bm.exempt.override.muteip") && onlinePlayer.hasPermission("bm.exempt.muteip")) {
        Message.SENDER_ERROR_EXEMPT.send(sender, "player", onlinePlayer.getName());
        return CommandResult.SUCCESS;
      }
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], true);
    } catch (Exception e1) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.IP_MUTE, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    final long expires = expiresCheck;
    final String reason = parser.getReason().getMessage();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final Long ip = CommandUtils.getIp(ipStr);

      if (ip == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", ipStr);
        return;
      }

      final boolean isMuted = plugin.getIpMuteStorage().isMuted(ip);

      if (isMuted && !sender.hasPermission("bm.command.tempmuteip.override")) {
        Message.MUTEIP_ERROR_EXISTS.send(sender, "ip", ipStr);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;
      if (isMuted) {
        IpMuteData mute = plugin.getIpMuteStorage().getMute(ip);

        if (mute != null) {
          try {
            plugin.getIpMuteStorage().unmute(mute, actor);
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
            e.printStackTrace();
            return;
          }
        }
      }

      final IpMuteData mute = new IpMuteData(ip, actor, reason, isSoft, expires);
      boolean created;

      try {
        created = plugin.getIpMuteStorage().mute(mute, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.MUTEIP_ERROR_EXISTS.asString(plugin.getLocaleManager(),"ip", ipStr));
        return;
      }

      if (!created) return;
      if (isSoft) return;

      // Find online players
      plugin.getBootstrap().getScheduler().executeSync(() -> {
        String message = Message.TEMPMUTEIP_IP_DISALLOWED.asString(plugin.getLocaleManager(),
                                 "reason", mute.getReason(),
                                 "actor", actor.getName(),
                                 "expires", DateUtils.getDifferenceFormat(mute.getExpires()));

        for (UUID onlineUUID : plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList())) {
          plugin.getBootstrap().getPlayerAsSender(onlineUUID).ifPresent(target -> {
            if(IPUtils.toLong(target.getIPAddress()) == ip) {
              target.sendMessage(message);
            }
          });
        }

      });

    });

    return CommandResult.SUCCESS;
  }

}
