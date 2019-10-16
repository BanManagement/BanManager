package me.confuser.banmanager.common.commands.global;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class MuteAllCommand extends CommonCommand {

  public MuteAllCommand(BanManagerPlugin plugin) {
    super(plugin, "muteall");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(getPermission() + ".soft")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.getArgs().length < 2) {
      return false;
    }

    if (parser.getArgs()[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parser.getArgs()[0];
    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      final PlayerData actor = sender.getData();

      if (actor == null) return;

      final GlobalPlayerMuteData ban = new GlobalPlayerMuteData(player, actor, reason, isSoft);
      int created;

      try {
        created = getPlugin().getGlobalPlayerMuteStorage().create(ban);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (created != 1) {
        return;
      }

      Message.get("muteall.notify")
             .set("actor", ban.getActorName())
             .set("reason", ban.getReason())
             .set("player", player.getName())
             .set("playerId", player.getUUID().toString())
             .sendTo(sender);
    });

    return true;
  }

}
