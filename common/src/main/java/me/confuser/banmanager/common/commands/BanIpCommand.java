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
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.parsers.Reason;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BanIpCommand extends SingleCommand {

  public BanIpCommand(LocaleManager locale) {
    super(CommandSpec.BANIP.localize(locale), "banip", CommandPermission.BANIP, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) {
    CommandParser parser = new CommandParser((String[]) argsIn.toArray(), 1);
    String[] args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(this.getPermission().get().getPermission() + ".silent")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 2) {
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
      Optional<Sender> onlineTargetOpt = plugin.getBootstrap().getPlayerAsSender(ipStr);
      if(onlineTargetOpt.isPresent()) {
        Sender onlineTarget = onlineTargetOpt.get();
        if(!sender.hasPermission("bm.exempt.override.banip") && onlineTarget.hasPermission("bm.exempt.banip")) {
          Message.SENDER_ERROR_EXEMPT.send(sender, "player", onlineTarget.getName());
          return CommandResult.SUCCESS;
        }
      }
    }

    final Reason reason = parser.getReason();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final Long ip = CommandUtils.getIp(ipStr);

      if (ip == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", ipStr);
        return;
      }

      final boolean isBanned = plugin.getIpBanStorage().isBanned(ip);

      if (isBanned && !sender.hasPermission("bm.command.banip.override")) {
        Message.BANIP_ERROR_EXISTS.send(sender, "ip", ipStr);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      if (isBanned) {
        IpBanData ban = plugin.getIpBanStorage().getBan(ip);

        if (ban != null) {
          try {
            plugin.getIpBanStorage().unban(ban, actor);
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
            e.printStackTrace();
            return;
          }
        }
      }

      final IpBanData ban = new IpBanData(ip, actor, reason.getMessage());
      boolean created;

      try {
        created = plugin.getIpBanStorage().ban(ban, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.BANIP_ERROR_EXISTS.asString(plugin.getLocaleManager(),"ip", ipStr));
        return;
      }

      if (!created) {
        return;
      }

      // Find online players
      plugin.getBootstrap().getScheduler().executeSync(() -> {
        String kickMessage = Message.BANIP_IP_KICK.asString(plugin.getLocaleManager(),
                                     "reason", ban.getReason(),
                                     "actor", actor.getName());

        for (UUID onlineUUID : plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList())) {
          plugin.getBootstrap().getPlayerAsSender(onlineUUID).ifPresent(target -> {
            if(IPUtils.toLong(target.getIPAddress()) == ip) {
              target.kick(kickMessage);
            }
          });
        }

      });
    });

    return CommandResult.SUCCESS;
  }

}
