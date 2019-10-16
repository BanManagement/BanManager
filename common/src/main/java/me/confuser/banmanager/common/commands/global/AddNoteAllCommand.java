package me.confuser.banmanager.common.commands.global;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalPlayerNoteData;
import me.confuser.banmanager.common.util.Message;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;

public class AddNoteAllCommand extends CommonCommand {

  public AddNoteAllCommand(BanManagerPlugin plugin) {
    super(plugin, "addnoteall");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.getArgs().length < 2) {
      return false;
    }

    if (parser.getArgs()[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parser.getArgs()[0];
    final String message = StringUtils.join(parser.getArgs(), " ", 1, parser.getArgs().length);

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      final GlobalPlayerNoteData note = new GlobalPlayerNoteData(player, actor, message);
      int created;

      try {
        created = getPlugin().getGlobalPlayerNoteStorage().create(note);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (created != 1) {
        return;
      }

      Message.get("addnoteall.notify")
             .set("actor", note.getActorName())
             .set("message", note.getMessageColours())
             .set("player", player.getName())
             .set("playerId", player.getUUID().toString())
             .sendTo(sender);
    });

    return true;
  }
}
