package me.confuser.banmanager.commands;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class RollbackCommand extends AutoCompleteNameTabCommand<BanManager> {

  // Must be ArrayList as needs to execute in this order
  private static ArrayList<String> types = new ArrayList<String>() {

    {
      add("bans");
      add("banrecords");
      add("ipbans");
      add("ipbanrecords");
      add("ipmutes");
      add("ipmuterecords");
      add("kicks");
      add("mutes");
      add("muterecords");
      add("notes");
      add("reports");
      add("warnings");
    }
  };

  public RollbackCommand() {
    super("bmrollback");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, final String[] args) {

    if (args.length < 2) return false;

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args[1], false);
    } catch (Exception e) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (plugin.getConfiguration().getTimeLimits().isPastLimit(sender, TimeLimitType.ROLLBACK, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;

    // Check if UUID vs name
    final String playerName = args[0];

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData player = CommandUtils.getPlayer(sender, playerName);

        if (player == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        ArrayList<String> types = new ArrayList<>();

        if (args.length == 2) {
          for (String type : RollbackCommand.types) {
            if (!sender.hasPermission("bm.command.bmrollback." + type)) {
              Message.get("sender.error.noPermission").sendTo(sender);
              return;
            }
          }

          types.addAll(RollbackCommand.types);
        } else {
          for (int i = 2; i < args.length; i++) {
            String type = args[1].toLowerCase();

            if (type.contains(",")) {
              types.addAll(Arrays.asList(type.split(",")));
            } else {
              types.add(type);
            }
          }
        }

        long now = System.currentTimeMillis() / 1000L;

        for (String type : types) {
          if (!RollbackCommand.types.contains(type)) {
            Message.get("bmrollback.error.invalid").set("type", type).sendTo(sender);
            return;
          } else if (sender.hasPermission("bm.command.bmrollback." + type)) {
            try {
              plugin.getRollbackStorage()
                    .create(new RollbackData(player, CommandUtils.getActor(sender), type, expires, now));
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        // Forces running in order
        // I.e bans must be executed before banrecords etc
        for (String type : RollbackCommand.types) {
          if (!types.contains(type)) continue;

          // @TODO Transactions for robustness
          try {
            switch (type) {
              case "bans":
                DeleteBuilder<PlayerBanData, Integer> bans = plugin.getPlayerBanStorage().deleteBuilder();
                bans.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
                bans.delete();
                break;

              case "banrecords":
                QueryBuilder<PlayerBanRecord, Integer> banRecords = plugin.getPlayerBanRecordStorage().queryBuilder();
                banRecords.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);

                for (PlayerBanRecord record : banRecords.query()) {
                  try {
                    if (plugin.getPlayerBanStorage().retrieveBan(record.getPlayer().getUUID()) == null) {
                      plugin.getPlayerBanStorage().create(new PlayerBanData(record));
                    }

                    plugin.getPlayerBanRecordStorage().delete(record);
                  } catch (SQLException e) {
                    sender.sendMessage(Message.get("sender.error.exception").toString());
                    e.printStackTrace();
                    return;
                  }
                }

                break;

              case "ipbans":
                DeleteBuilder<IpBanData, Integer> ipBans = plugin.getIpBanStorage().deleteBuilder();
                ipBans.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
                ipBans.delete();
                break;

              case "ipbanrecords":
                QueryBuilder<IpBanRecord, Integer> ipBanRecords = plugin.getIpBanRecordStorage().queryBuilder();
                ipBanRecords.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);

                for (IpBanRecord record : ipBanRecords.query()) {
                  try {
                    if (plugin.getIpBanStorage().retrieveBan(record.getIp()) == null) {
                      plugin.getIpBanStorage().create(new IpBanData(record));
                    }

                    plugin.getIpBanRecordStorage().delete(record);
                  } catch (SQLException e) {
                    sender.sendMessage(Message.get("sender.error.exception").toString());
                    e.printStackTrace();
                    return;
                  }
                }

                break;

              case "kicks":
                DeleteBuilder<PlayerKickData, Integer> kicks = plugin.getPlayerKickStorage().deleteBuilder();
                kicks.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
                kicks.delete();

                break;

              case "mutes":
                DeleteBuilder<PlayerMuteData, Integer> mutes = plugin.getPlayerMuteStorage().deleteBuilder();
                mutes.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
                mutes.delete();
                break;

              case "muterecords":
                QueryBuilder<PlayerMuteRecord, Integer> muteRecords = plugin.getPlayerMuteRecordStorage().queryBuilder();
                muteRecords.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);

                for (PlayerMuteRecord record : muteRecords.query()) {
                  try {
                    if (plugin.getPlayerMuteStorage().retrieveMute(record.getPlayer().getUUID()) == null) {
                      plugin.getPlayerMuteStorage().create(new PlayerMuteData(record));
                    }

                    plugin.getPlayerMuteRecordStorage().delete(record);
                  } catch (SQLException e) {
                    sender.sendMessage(Message.get("sender.error.exception").toString());
                    e.printStackTrace();
                    return;
                  }
                }
                break;

              case "ipmutes":
                DeleteBuilder<IpMuteData, Integer> ipMutes = plugin.getIpMuteStorage().deleteBuilder();
                ipMutes.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
                ipMutes.delete();
                break;

              case "ipmuterecords":
                QueryBuilder<IpMuteRecord, Integer> ipMuteRecords = plugin.getIpMuteRecordStorage().queryBuilder();
                ipMuteRecords.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);

                for (IpMuteRecord record : ipMuteRecords.query()) {
                  try {
                    if (plugin.getIpMuteStorage().retrieveMute(record.getIp()) == null) {
                      plugin.getIpMuteStorage().create(new IpMuteData(record));
                    }

                    plugin.getIpMuteRecordStorage().delete(record);
                  } catch (SQLException e) {
                    sender.sendMessage(Message.get("sender.error.exception").toString());
                    e.printStackTrace();
                    return;
                  }
                }
                break;

              case "notes":
                DeleteBuilder<PlayerNoteData, Integer> notes = plugin.getPlayerNoteStorage().deleteBuilder();
                notes.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
                notes.delete();
                break;

              case "reports":
                QueryBuilder<PlayerReportData, Integer> reports = plugin.getPlayerReportStorage().queryBuilder();
                reports.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);

                for (PlayerReportData record : reports.query()) {
                  plugin.getPlayerReportStorage().deleteById(record.getId());
                }
                break;

              case "warnings":
                DeleteBuilder<PlayerWarnData, Integer> warnings = plugin.getPlayerWarnStorage().deleteBuilder();
                warnings.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
                warnings.delete();
                break;
            }
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }

          Message.get("bmrollback.notify")
                 .set("type", type)
                 .set("player", player.getName())
                 .set("playerId", player.getUUID().toString())
                 .sendTo(sender);
        }
      }
    });

    return true;
  }

}
