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
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.util.CommandUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.List;

public class AddNoteCommand extends SingleCommand {

  public AddNoteCommand(LocaleManager locale) {
    super(CommandSpec.ADDNOTE.localize(locale), "addnote", CommandPermission.ADDNOTE, Predicates.alwaysFalse());
  }

  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 2) {
      return CommandResult.INVALID_ARGS;
    }

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, this.getName(), (String[]) args.toArray());
      return CommandResult.SUCCESS;
    }

    if (args.get(0).equalsIgnoreCase(sender.getName())) {
      Message.SENDER_ERROR_NOSELF.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    // Check if UUID vs name
    final String playerName = args.get(0);
    final String message = StringUtils.join(args, " ", 1, args.size());

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", player);
        return;
      }

      final PlayerData actor = CommandUtils.getActor(sender);
      final PlayerNoteData warning = new PlayerNoteData(player, actor, message);

      try {
        plugin.getPlayerNoteStorage().addNote(warning);
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
      }

    });

    return CommandResult.SUCCESS;
  }

}
