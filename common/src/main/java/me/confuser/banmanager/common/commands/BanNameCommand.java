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
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.parsers.Reason;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BanNameCommand extends SingleCommand {

  public BanNameCommand(LocaleManager locale) {
    super(CommandSpec.BANNAME.localize(locale), "banname", CommandPermission.BANNAME, Predicates.alwaysFalse());
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

    final String name = args[0];
    final Reason reason = parser.getReason();

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final boolean isBanned = plugin.getNameBanStorage().isBanned(name);

      if (isBanned && !sender.hasPermission("bm.command.banname.override")) {
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

      final NameBanData ban = new NameBanData(name, actor, reason.getMessage());
      boolean created;

      try {
        created = plugin.getNameBanStorage().ban(ban, isSilent);
      } catch (SQLException e) {
        CommandUtils.handlePunishmentCreateException(e, sender, Message.BANNAME_ERROR_EXISTS.asString(plugin.getLocaleManager(),"name", name));
        return;
      }

      if (!created) {
        return;
      }

      // Find online players
      plugin.getBootstrap().getScheduler().executeSync(() -> {
        String kickMessage = Message.BANNAME_NAME_KICK.asString(plugin.getLocaleManager(),
                                     "reason", ban.getReason(),
                                     "actor", actor.getName(),
                                     "name", name);

        for (UUID onlineUUID : plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList())) {
          plugin.getBootstrap().getPlayerAsSender(onlineUUID).ifPresent(onlinePlayer -> {
            if(onlinePlayer.getName().equalsIgnoreCase(name)) {
              onlinePlayer.kick(kickMessage);
            }
          });
        }

      });
    });

    return CommandResult.SUCCESS;
  }

}
