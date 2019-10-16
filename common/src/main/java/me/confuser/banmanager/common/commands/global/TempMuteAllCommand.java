package me.confuser.banmanager.common.commands.global;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class TempMuteAllCommand extends CommonCommand {

  public TempMuteAllCommand(BanManagerPlugin plugin) {
    super(plugin, "tempmuteall");
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSoft = parser.isSoft();

    if (isSoft && !sender.hasPermission(getPermission() + ".soft")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.getArgs().length < 3) {
      return false;
    }

    if (parser.getArgs()[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    // Check if UUID vs name
    final String playerName = parser.getArgs()[0];
    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parser.getArgs()[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("invalidTime").toString());
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.PLAYER_MUTE, expiresCheck)) {
      Message.get("timeLimitError").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;

    final String reason = parser.getReason(2).getMessage();

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      final PlayerData actor = sender.getData();
      final GlobalPlayerMuteData mute = new GlobalPlayerMuteData(player, actor, reason, isSoft, expires);
      int created;

      try {
        created = getPlugin().getGlobalPlayerMuteStorage().create(mute);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (created != 1) {
        return;
      }

      Message.get("tempmuteall.notify")
             .set("actor", mute.getActorName())
             .set("reason", mute.getReason())
             .set("expires", DateUtils.getDifferenceFormat(mute.getExpires()))
             .set("player", player.getName())
             .set("playerId", player.getUUID().toString())
             .sendTo(sender);
    });

    return true;
  }

}
