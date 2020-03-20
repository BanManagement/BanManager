package me.confuser.banmanager.common.commands;


import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.TimeLimitType;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class TempNameBanCommand extends CommonCommand {

  public TempNameBanCommand(BanManagerPlugin plugin) {
    super(plugin, "tempbanname", true, 2);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 3) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    long expiresCheck;

    try {
      expiresCheck = DateUtils.parseDateDiff(parser.args[1], true);
    } catch (Exception e1) {
      sender.sendMessage(Message.get("time.error.invalid").toString());
      return true;
    }

    if (getPlugin().getConfig().getTimeLimits().isPastLimit(sender, TimeLimitType.NAME_BAN, expiresCheck)) {
      Message.get("time.error.limit").sendTo(sender);
      return true;
    }

    final String name = parser.args[0];
    final long expires = expiresCheck;
    final String reason = parser.getReason().getMessage();

    getPlugin().getScheduler().runAsync(new Runnable() {

      @Override
      public void run() {
        final boolean isBanned = getPlugin().getNameBanStorage().isBanned(name);

        if (isBanned && !sender.hasPermission("bm.command.tempbanname.override")) {
          Message message = Message.get("banname.error.exists");
          message.set("name", name);

          sender.sendMessage(message.toString());
          return;
        }

        final PlayerData actor = sender.getData();

        if (actor == null) return;

        if (isBanned) {
          NameBanData ban = getPlugin().getNameBanStorage().getBan(name);

          if (ban != null) {
            try {
              getPlugin().getNameBanStorage().unban(ban, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        final NameBanData ban = new NameBanData(name, actor, reason, isSilent, expires);
        boolean created;

        try {
          created = getPlugin().getNameBanStorage().ban(ban);
        } catch (SQLException e) {
          handlePunishmentCreateException(e, sender, Message.get("banname.error.exists").set("player",
              name));
          return;
        }

        if (!created) {
          return;
        }

        // Find online players
        getPlugin().getScheduler().runSync(() -> {
          Message kickMessage = Message.get("tempbanname.name.kick")
              .set("reason", ban.getReason())
              .set("name", name)
              .set("actor", actor.getName());

          for (CommonPlayer onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
            if (onlinePlayer.getName().equalsIgnoreCase(name)) {
              onlinePlayer.kick(kickMessage.toString());
            }
          }
        });

      }

    });

    return true;
  }
}
