package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.Message;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;

public class AddNoteCommand extends CommonCommand {

  public AddNoteCommand(BanManagerPlugin plugin) {
    super(plugin, "addnote");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 2) {
      return false;
    }

    if (parser.args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parser.args[0];
    final boolean isUUID = playerName.length() > 16;
    final String message = StringUtils.join(parser.args, " ", 1, parser.args.length);

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      final PlayerData actor = sender.getData();
      final PlayerNoteData warning = new PlayerNoteData(player, actor, message);

      try {
        getPlugin().getPlayerNoteStorage().addNote(warning);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
      }
    });

    return true;
  }
}
