package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;

import java.sql.SQLException;

public class UnbanNameCommand extends CommonCommand {

  public UnbanNameCommand(BanManagerPlugin plugin) {
    super(plugin, "unbanname", true);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser parser) {
    if (parser.args.length < 1) {
      return false;
    }

    final String name = parser.args[0];
    final String reason = parser.args.length > 1 ? parser.getReason(1).getMessage() : "";

    getPlugin().getScheduler().runAsync(() -> {
      if (!getPlugin().getNameBanStorage().isBanned(name)) {
        Message message = Message.get("unbanname.error.noExists");
        message.set("name", name);

        sender.sendMessage(message.toString());
        return;
      }

      NameBanData ban = getPlugin().getNameBanStorage().getBan(name);
      final PlayerData actor = sender.getData();

      if (actor == null) return;

      boolean unbanned;

      try {
        unbanned = getPlugin().getNameBanStorage().unban(ban, actor, reason);
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
        return;
      }

      if (!unbanned) {
        return;
      }

      Message message = Message.get("unbanname.notify");
      message
              .set("name", name)
              .set("actor", actor.getName())
              .set("reason", reason);

      if (!sender.hasPermission("bm.notify.unbanname")) {
        message.sendTo(sender);
      }

      getPlugin().getServer().broadcast(message.toString(), "bm.notify.unbanname");
    });

    return true;
  }
}
