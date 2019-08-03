package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;

public class UnbanNameCommand extends BukkitCommand<BanManager> {

  public UnbanNameCommand() {
    super("unbanname");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length < 1) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    final String name = args[0];
    final String reason = args.length > 1 ? CommandUtils.getReason(1, args).getMessage() : "";

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        if (!plugin.getNameBanStorage().isBanned(name)) {
          Message message = Message.get("unbanname.error.noExists");
          message.set("name", name);

          sender.sendMessage(message.toString());
          return;
        }

        NameBanData ban = plugin.getNameBanStorage().getBan(name);
        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        boolean unbanned;

        try {
          unbanned = plugin.getNameBanStorage().unban(ban, actor, reason);
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

        CommandUtils.broadcast(message.toString(), "bm.notify.unbanname");
      }

    });

    return true;
  }
}
