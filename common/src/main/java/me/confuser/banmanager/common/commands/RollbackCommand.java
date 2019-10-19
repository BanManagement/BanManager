package me.confuser.banmanager.common.commands;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

public class RollbackCommand extends CommonCommand {

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

  public RollbackCommand(BanManagerPlugin plugin) {
    super(plugin, "bmrollback", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 2) return false;

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parser.args[1], false);
    } catch (Exception e) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.ROLLBACK, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final long expires = expiresCheck;

    // Check if UUID vs name
    final String playerName = parser.args[0];

    getPlugin().getScheduler().runAsync(() -> {
      final PlayerData player = getPlayer(sender, playerName, false);

      if (player == null) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return;
      }

      ArrayList<String> types = new ArrayList<>();

      if (parser.args.length == 2) {
        for (String type : RollbackCommand.types) {
          if (!sender.hasPermission("bm.command.bmrollback." + type)) {
            Message.get("sender.error.noPermission").sendTo(sender);
            return;
          }
        }

        types.addAll(RollbackCommand.types);
      } else {
        for (int i = 2; i < parser.args.length; i++) {
          String type = parser.args[1].toLowerCase();

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
            getPlugin().getRollbackStorage()
                .create(new RollbackData(player, sender.getData(), type, expires, now));
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
              DeleteBuilder<PlayerBanData, Integer> bans = getPlugin().getPlayerBanStorage().deleteBuilder();
              bans.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
              bans.delete();
              break;

            case "banrecords":
              QueryBuilder<PlayerBanRecord, Integer> banRecords = getPlugin().getPlayerBanRecordStorage()
                  .queryBuilder();
              banRecords.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);

              for (PlayerBanRecord record : banRecords.query()) {
                try {
                  if (getPlugin().getPlayerBanStorage().retrieveBan(record.getPlayer().getUUID()) == null) {
                    getPlugin().getPlayerBanStorage().create(new PlayerBanData(record));
                  }

                  getPlugin().getPlayerBanRecordStorage().delete(record);
                } catch (SQLException e) {
                  sender.sendMessage(Message.get("sender.error.exception").toString());
                  e.printStackTrace();
                  return;
                }
              }

              break;

            case "ipbans":
              DeleteBuilder<IpBanData, Integer> ipBans = getPlugin().getIpBanStorage().deleteBuilder();
              ipBans.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
              ipBans.delete();
              break;

            case "ipbanrecords":
              QueryBuilder<IpBanRecord, Integer> ipBanRecords = getPlugin().getIpBanRecordStorage().queryBuilder();
              ipBanRecords.where().eq("actor_id", player.getId()).and().le("created", now).and()
                  .ge("created", expires);

              for (IpBanRecord record : ipBanRecords.query()) {
                try {
                  if (getPlugin().getIpBanStorage().retrieveBan(record.getIp()) == null) {
                    getPlugin().getIpBanStorage().create(new IpBanData(record));
                  }

                  getPlugin().getIpBanRecordStorage().delete(record);
                } catch (SQLException e) {
                  sender.sendMessage(Message.get("sender.error.exception").toString());
                  e.printStackTrace();
                  return;
                }
              }

              break;

            case "kicks":
              DeleteBuilder<PlayerKickData, Integer> kicks = getPlugin().getPlayerKickStorage().deleteBuilder();
              kicks.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
              kicks.delete();

              break;

            case "mutes":
              DeleteBuilder<PlayerMuteData, Integer> mutes = getPlugin().getPlayerMuteStorage().deleteBuilder();
              mutes.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
              mutes.delete();
              break;

            case "muterecords":
              QueryBuilder<PlayerMuteRecord, Integer> muteRecords = getPlugin().getPlayerMuteRecordStorage()
                  .queryBuilder();
              muteRecords.where().eq("actor_id", player.getId()).and().le("created", now).and()
                  .ge("created", expires);

              for (PlayerMuteRecord record : muteRecords.query()) {
                try {
                  if (getPlugin().getPlayerMuteStorage().retrieveMute(record.getPlayer().getUUID()) == null) {
                    getPlugin().getPlayerMuteStorage().create(new PlayerMuteData(record));
                  }

                  getPlugin().getPlayerMuteRecordStorage().delete(record);
                } catch (SQLException e) {
                  sender.sendMessage(Message.get("sender.error.exception").toString());
                  e.printStackTrace();
                  return;
                }
              }
              break;

            case "ipmutes":
              DeleteBuilder<IpMuteData, Integer> ipMutes = getPlugin().getIpMuteStorage().deleteBuilder();
              ipMutes.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
              ipMutes.delete();
              break;

            case "ipmuterecords":
              QueryBuilder<IpMuteRecord, Integer> ipMuteRecords = getPlugin().getIpMuteRecordStorage().queryBuilder();
              ipMuteRecords.where().eq("actor_id", player.getId()).and().le("created", now).and()
                  .ge("created", expires);

              for (IpMuteRecord record : ipMuteRecords.query()) {
                try {
                  if (getPlugin().getIpMuteStorage().retrieveMute(record.getIp()) == null) {
                    getPlugin().getIpMuteStorage().create(new IpMuteData(record));
                  }

                  getPlugin().getIpMuteRecordStorage().delete(record);
                } catch (SQLException e) {
                  sender.sendMessage(Message.get("sender.error.exception").toString());
                  e.printStackTrace();
                  return;
                }
              }
              break;

            case "notes":
              DeleteBuilder<PlayerNoteData, Integer> notes = getPlugin().getPlayerNoteStorage().deleteBuilder();
              notes.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);
              notes.delete();
              break;

            case "reports":
              QueryBuilder<PlayerReportData, Integer> reports = getPlugin().getPlayerReportStorage().queryBuilder();
              reports.where().eq("actor_id", player.getId()).and().le("created", now).and().ge("created", expires);

              for (PlayerReportData record : reports.query()) {
                getPlugin().getPlayerReportStorage().deleteById(record.getId());
              }
              break;

            case "warnings":
              DeleteBuilder<PlayerWarnData, Integer> warnings = getPlugin().getPlayerWarnStorage().deleteBuilder();
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
    });

    return true;
  }

}
