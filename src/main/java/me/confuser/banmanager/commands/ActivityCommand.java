package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class ActivityCommand extends AutoCompleteNameTabCommand<BanManager> {

  public ActivityCommand() {
    super("bmactivity");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {

    if (args.length == 0 || args.length > 2) {
      return false;
    }

    long sinceCheck;

    try {
      sinceCheck = DateUtils.parseDateDiff(args[0], false);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    final long since = sinceCheck;

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        ArrayList<HashMap<String, Object>> results = null;
        String messageType = "bmactivity.row.all";

        if (args.length == 2) {
          messageType = "bmactivity.row.player";
          PlayerData player = plugin.getPlayerStorage().retrieve(args[1], false);

          if (player == null) {
            sender.sendMessage(Message.get("sender.error.notFound").set("player", args[1]).toString());
            return;
          }

          results = plugin.getActivityStorage().getSince(since, player);
        } else {
          results = plugin.getActivityStorage().getSince(since);
        }

        if (results == null || results.size() == 0) {
          Message.get("bmactivity.noResults").sendTo(sender);
          return;
        }

        String dateTimeFormat = Message.getString("bmactivity.row.dateTimeFormat");
        SimpleDateFormat dateFormatter = new SimpleDateFormat(dateTimeFormat);

        for (HashMap<String, Object> result : results) {
          Message message = Message.get(messageType)
                                   .set("player", (String) result.get("player"))
                                   .set("type", (String) result.get("type"))
                                   .set("created", dateFormatter
                                           .format(new java.util.Date((long) result.get("created") * 1000L)));

          if (result.get("actor") != null) message.set("actor", (String) result.get("actor"));

          message.sendTo(sender);
        }
      }

    });

    return true;
  }
}
