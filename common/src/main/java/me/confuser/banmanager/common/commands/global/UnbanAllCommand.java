package me.confuser.banmanager.common.commands.global;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.global.GlobalPlayerBanRecordData;
import me.confuser.banmanager.common.runnables.GlobalLocalApplyHelper;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.UUID;

public class UnbanAllCommand extends CommonCommand {

  public UnbanAllCommand(BanManagerPlugin plugin) {
    super(plugin, "unbanall", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.getArgs().length < 1) {
      return false;
    }

    // Check if UUID vs name
    final String playerName = parser.getArgs()[0];
    final boolean isUUID = playerName.length() > 16;
    boolean isBanned;

    if (isUUID) {
      isBanned = getPlugin().getPlayerBanStorage().isBanned(UUID.fromString(playerName));
    } else {
      isBanned = getPlugin().getPlayerBanStorage().isBanned(playerName);
    }

    if (!isBanned) {
      Message message = Message.get("unban.error.noExists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      PlayerBanData ban;

      if (isUUID) {
        ban = getPlugin().getPlayerBanStorage().getBan(UUID.fromString(playerName));
      } else {
        ban = getPlugin().getPlayerBanStorage().getBan(playerName);
      }

      if (ban == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      PlayerData actor = sender.getData();
      GlobalPlayerBanRecordData record = new GlobalPlayerBanRecordData(ban.getPlayer(), actor);

      int unbanned;

      try {
        unbanned = getPlugin().getGlobalPlayerBanRecordStorage().create(record);
        if (unbanned > 0) {
          new GlobalLocalApplyHelper(getPlugin()).applyUnban(record, false);
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.get("errorOccurred").toString());
        e.printStackTrace();
        return;
      }

      if (unbanned == 0) {
        return;
      }

      Message.get("unbanall.notify")
             .set("actor", actor.getName())
             .set("player", ban.getPlayer().getName())
             .set("playerId", ban.getPlayer().getUUID().toString())
             .sendTo(sender);
    });

    return true;
  }
}
