package me.confuser.banmanager.common.commands;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import me.confuser.banmanager.common.command.CommandResult;
import me.confuser.banmanager.common.command.abstraction.SingleCommand;
import me.confuser.banmanager.common.command.access.CommandPermission;
import me.confuser.banmanager.common.locale.LocaleManager;
import me.confuser.banmanager.common.locale.command.CommandSpec;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.common.util.Predicates;
import me.confuser.banmanager.configs.TimeLimitType;
import me.confuser.banmanager.data.*;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.DateUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RollbackCommand extends SingleCommand {

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

  public RollbackCommand(LocaleManager locale) {
    super(CommandSpec.BMROLLBACK.localize(locale), "bmrollback", CommandPermission.BMROLLBACK, Predicates.alwaysFalse());
  }


  @Override
  public CommandResult execute(BanManagerPlugin plugin, Sender sender, List<String> args, String label) {
    if (args.size() < 2) return CommandResult.INVALID_ARGS;

    if (CommandUtils.isValidNameDelimiter(args.get(0))) {
      CommandUtils.handleMultipleNames(sender, this.getName(), (String[]) args.toArray());
      return CommandResult.SUCCESS;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(args.get(1), false);
    } catch (Exception e) {
      Message.TIME_ERROR_INVALID.send(sender);
      return CommandResult.INVALID_ARGS;
    }

    if (plugin.getTimeLimits().isPastLimit(sender, TimeLimitType.ROLLBACK, expiresCheck)) {
      Message.TIME_ERROR_LIMIT.send(sender);
      return CommandResult.SUCCESS;
    }

    final long expires = expiresCheck;

    // Check if UUID vs name
    final String playerName = args.get(0);

    plugin.getBootstrap().getScheduler().executeAsync(() -> {
      final PlayerData player = CommandUtils.getPlayer(sender, playerName);

      if (player == null) {
        Message.SENDER_ERROR_NOT_FOUND.send(sender, "player", playerName);
        return;
      }

      ArrayList<String> types = new ArrayList<>();

      if (args.size() == 2) {
        for (String type : RollbackCommand.types) {
          if (!sender.hasPermission("bm.command.bmrollback." + type)) {
            Message.SENDER_ERROR_NOPERMISSION.send(sender);
            return;
          }
        }

        types.addAll(RollbackCommand.types);
      } else {
        for (int i = 2; i < args.size(); i++) {
          String type = args.get(1).toLowerCase();

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
          Message.BMROLLBACK_ERROR_INVALID.send(sender, "type", type);
          return;
        } else if (sender.hasPermission("bm.command.bmrollback." + type)) {
          try {
            plugin.getRollbackStorage().create(new RollbackData(player, CommandUtils.getActor(sender), type, expires, now));
          } catch (SQLException e) {
            Message.SENDER_ERROR_EXCEPTION.send(sender);
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
                  Message.SENDER_ERROR_EXCEPTION.send(sender);
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
                  Message.SENDER_ERROR_EXCEPTION.send(sender);
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
                  Message.SENDER_ERROR_EXCEPTION.send(sender);
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
                  Message.SENDER_ERROR_EXCEPTION.send(sender);
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
          Message.SENDER_ERROR_EXCEPTION.send(sender);
          e.printStackTrace();
          return;
        }

        Message.BMROLLBACK_NOTIFY.send(sender,
               "type", type,
               "player", player.getName(),
               "playerId", player.getUUID().toString());
      }
    });

    return CommandResult.SUCCESS;
  }

}
