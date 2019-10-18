package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class DeleteLastWarningCommand extends CommonCommand {

  public DeleteLastWarningCommand(BanManagerPlugin plugin) {
    super(plugin, "dwarn", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    // Check if UUID vs name
    final String playerName = parser.args[0];
    final boolean isUUID = playerName.length() > 16;

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, true);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      int updated = 0;
      try {
        updated = getPlugin().getPlayerWarnStorage().deleteRecent(player);
      } catch (SQLException e) {
        e.printStackTrace();
      }

      if (updated == 0) {
        Message.get("dwarn.error.noWarnings").set("player", player.getName()).sendTo(sender);
      } else {
        Message.get("dwarn.notify").set("player", player.getName()).set("actor", sender.getName()).sendTo(sender);

        CommonPlayer commonPlayer = getPlugin().getServer().getPlayer(player.getUUID());

        if (commonPlayer == null) return;

        commonPlayer.sendMessage(Message.get("dwarn.player.notify")
                                        .set("player", player.getName())
                                        .set("playerId", player.getUUID().toString())
                                        .set("actor", sender.getName()));
      }
    });

    return true;
  }
}
