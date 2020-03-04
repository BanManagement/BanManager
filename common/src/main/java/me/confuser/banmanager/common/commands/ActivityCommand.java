package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActivityCommand extends CommonCommand {

  public ActivityCommand(BanManagerPlugin plugin) {
    super(plugin, "bmactivity", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length == 0 || parser.args.length > 2) {
      return false;
    }

    long sinceCheck;

    try {
      sinceCheck = DateUtils.parseDateDiff(parser.args[0], false);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid"));
      return true;
    }

    final long since = sinceCheck;

    getPlugin().getScheduler().runAsync(() -> {
      List<Map<String, Object>> results;
      String messageType = "bmactivity.row.all";

      if (parser.args.length == 2) {
        messageType = "bmactivity.row.player";

        PlayerData player = null;
        final boolean isUUID = parser.args[1].length() > 16;

        if (isUUID) {
          try {
            player = getPlugin().getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(parser.args[1])));
          } catch (SQLException e) {
            e.printStackTrace();
          }
        } else {
          player = getPlugin().getPlayerStorage().retrieve(parser.args[1], false);
        }

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", parser.args[1]).toString());
          return;
        }

        results = getPlugin().getActivityStorage().getSince(since, player);
      } else {
        results = getPlugin().getActivityStorage().getSince(since);
      }

      if (results == null || results.size() == 0) {
        Message.get("bmactivity.noResults").sendTo(sender);
        return;
      }

      String dateTimeFormat = Message.getString("bmactivity.row.dateTimeFormat");

      for (Map<String, Object> result : results) {
        Message message = Message.get(messageType)
            .set("player", (String) result.get("player"))
            .set("type", (String) result.get("type"))
            .set("created", DateUtils.format(dateTimeFormat, (long) result.get("created")));

        if (result.get("actor") != null) message.set("actor", (String) result.get("actor"));

        message.sendTo(sender);
      }
    });

    return true;
  }
}
