package me.confuser.banmanager.common.commands.global;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.commands.CommandParser;
import me.confuser.banmanager.common.commands.CommonCommand;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;
import me.confuser.banmanager.common.data.global.GlobalPlayerMuteRecordData;
import me.confuser.banmanager.common.runnables.GlobalLocalApplyHelper;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.UUID;

public class UnmuteAllCommand extends CommonCommand {

  public UnmuteAllCommand(BanManagerPlugin plugin) {
    super(plugin, "unmuteall", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.getArgs().length < 1) {
      return false;
    }

    // Check if UUID vs name
    final String playerName = parser.getArgs()[0];
    final boolean isUUID = playerName.length() > 16;
    boolean isMuted;

    if (isUUID) {
      isMuted = getPlugin().getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
    } else {
      isMuted = getPlugin().getPlayerMuteStorage().isMuted(playerName);
    }

    if (!isMuted) {
      Message message = Message.get("unmute.error.noExists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      PlayerMuteData mute;

      if (isUUID) {
        mute = getPlugin().getPlayerMuteStorage().getMute(UUID.fromString(playerName));
      } else {
        mute = getPlugin().getPlayerMuteStorage().getMute(playerName);
      }

      if (mute == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      PlayerData actor = sender.getData();
      GlobalPlayerMuteRecordData record = new GlobalPlayerMuteRecordData(mute.getPlayer(), actor);

      int unmuted;

      try {
        unmuted = getPlugin().getGlobalPlayerMuteRecordStorage().create(record);
        if (unmuted > 0) {
          new GlobalLocalApplyHelper(getPlugin()).applyUnmute(record, false);
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.get("errorOccurred").toString());
        e.printStackTrace();
        return;
      }

      if (unmuted == 0) {
        return;
      }

      Message.get("unmuteall.notify")
             .set("actor", actor.getName())
             .set("player", mute.getPlayer().getName())
             .set("playerId", mute.getPlayer().getUUID().toString())
             .sendTo(sender);
    });

    return true;
  }
}
