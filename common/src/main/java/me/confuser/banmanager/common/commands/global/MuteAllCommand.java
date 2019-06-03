package me.confuser.banmanager.common.commands.global;

import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.CommandException;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class MuteAllCommand extends SingleCommand {

  public MuteAllCommand(LocaleManager locale) {
    super(CommandSpec.MUTEALL.localize(locale), "muteall", CommandPermission.MUTEALL, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> argsIn, String label) throws CommandException {
    CommandParser parser = new CommandParser((String[]) argsIn.toArray());
    String[] args = parser.getArgs();

    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(this.getPermission().get().getPermission() + ".soft")) {
      Message.SENDER_ERROR_NOPERMISSION.send(sender);
      return CommandResult.NO_PERMISSION;
    }

    if (args.length < 2) {
      return CommandResult.INVALID_ARGS;
    }

    if (args[0].equalsIgnoreCase(sender.getName())) {
      Message.SENDER_ERROR_NOSELF.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String playerName = args[0];
    final String reason = StringUtils.join(args, " ", 1, args.length);

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender,"player", playerName);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);

      if (actor == null) return;

      final GlobalPlayerMuteData ban = new GlobalPlayerMuteData(player, actor, reason, isSoft);
      int created;

      try {
        created = plugin.getGlobalPlayerMuteStorage().create(ban);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
        return;
      }

      if (created != 1) {
        return;
      }

      Message.MUTEALL_NOTIFY.send(sender,
              "actor", ban.getActorName(),
              "reason", ban.getReason(),
              "player", player.getName(),
              "playerId", player.getUUID().toString());
    });

    return CommandResult.SUCCESS;
  }

}
