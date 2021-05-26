package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class BanNameCommand extends CommonCommand {

  public BanNameCommand(BanManagerPlugin plugin) {
    super(plugin, "banname", true, 1);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (parser.args.length < 2) {
      return false;
    }

    if (parser.isInvalidReason()) {
      Message.get("sender.error.invalidReason")
              .set("reason", parser.getReason().getMessage())
              .sendTo(sender);
      return true;
    }

    final String name = parser.args[0];
    final Reason reason = parser.getReason();

    getPlugin().getScheduler().runAsync(() -> {
      final boolean isBanned = getPlugin().getNameBanStorage().isBanned(name);

      if (isBanned && !sender.hasPermission("bm.command.banname.override")) {
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

      final NameBanData ban = new NameBanData(name, actor, reason.getMessage(), isSilent);
      boolean created;

      try {
        created = getPlugin().getNameBanStorage().ban(ban);
      } catch (SQLException e) {
        handlePunishmentCreateException(e, sender, Message.get("banname.error.exists").set("name",
            name));
        return;
      }

      if (!created) {
        return;
      }

      // Find online players
      getPlugin().getScheduler().runSync(() -> {
        Message kickMessage = Message.get("banname.name.kick")
            .set("reason", ban.getReason())
            .set("actor", actor.getName())
            .set("id", ban.getId())
            .set("name", name);

        for (CommonPlayer onlinePlayer : getPlugin().getServer().getOnlinePlayers()) {
          if (onlinePlayer.getName().equalsIgnoreCase(name)) {
            onlinePlayer.kick(kickMessage.toString());
          }
        }
      });
    });

    return true;
  }

}
