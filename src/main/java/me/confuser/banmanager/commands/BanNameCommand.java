package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.common.data.NameBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class BanNameCommand extends AutoCompleteNameTabCommand<BanManager> {

  public BanNameCommand() {
    super("banname");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    CommandParser parser = new CommandParser(args, 1);
    args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (args.length < 2) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    final String name = args[0];
    final Reason reason = parser.getReason();

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final boolean isBanned = plugin.getNameBanStorage().isBanned(name);

        if (isBanned && !sender.hasPermission("bm.command.banname.override")) {
          Message message = Message.get("banname.error.exists");
          message.set("name", name);

          sender.sendMessage(message.toString());
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        if (actor == null) return;

        if (isBanned) {
          NameBanData ban = plugin.getNameBanStorage().getBan(name);

          if (ban != null) {
            try {
              plugin.getNameBanStorage().unban(ban, actor);
            } catch (SQLException e) {
              sender.sendMessage(Message.get("sender.error.exception").toString());
              e.printStackTrace();
              return;
            }
          }
        }

        final NameBanData ban = new NameBanData(name, actor, reason.getMessage());
        boolean created;

        try {
          created = plugin.getNameBanStorage().ban(ban, isSilent);
        } catch (SQLException e) {
          CommandUtils.handlePunishmentCreateException(e, sender, Message.get("banname.error.exists").set("name",
                  name));
          return;
        }

        if (!created) {
          return;
        }

        // Find online players
        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          public void run() {
            Message kickMessage = Message.get("banname.name.kick")
                                         .set("reason", ban.getReason())
                                         .set("actor", actor.getName())
                                         .set("name", name);

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
              if (onlinePlayer.getName().equalsIgnoreCase(name)) {
                onlinePlayer.kickPlayer(kickMessage.toString());
              }
            }
          }
        });
      }

    });

    return true;
  }

}
