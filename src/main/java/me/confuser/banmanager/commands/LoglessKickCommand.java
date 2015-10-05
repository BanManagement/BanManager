package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandParser;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.banmanager.util.UUIDUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class LoglessKickCommand extends BukkitCommand<BanManager> {

  public LoglessKickCommand() {
    super("nlkick");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    CommandParser parser = new CommandParser(args);
    args = parser.getArgs();
    final boolean isSilent = parser.isSilent();

    if (isSilent && !sender.hasPermission(command.getPermission() + ".silent")) {
      sender.sendMessage(Message.getString("sender.error.noPermission"));
      return true;
    }

    if (args.length < 1) {
      return false;
    }

    if (CommandUtils.isValidNameDelimiter(args[0])) {
      CommandUtils.handleMultipleNames(sender, commandName, args);
      return true;
    }

    if (args[0].equalsIgnoreCase(sender.getName())) {
      sender.sendMessage(Message.getString("sender.error.noSelf"));
      return true;
    }

    String playerName = args[0];
    final Player player = plugin.getServer().getPlayer(playerName);

    if (player == null) {
      Message message = Message.get("sender.error.offline")
                               .set("[player]", playerName);

      sender.sendMessage(message.toString());
      return true;
    } else if (!sender.hasPermission("bm.exempt.override.kick") && player.hasPermission("bm.exempt.kick")) {
      Message.get("sender.error.exempt").set("player", player.getName()).sendTo(sender);
      return true;
    }

    final String reason = args.length > 1 ? CommandUtils.getReason(1, args) : "";

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerData actor;

        if (sender instanceof Player) {
          try {
            actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes((Player) sender));
          } catch (SQLException e) {
            sender.sendMessage(Message.get("sender.error.exception").toString());
            e.printStackTrace();
            return;
          }
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        final Message kickMessage;

        if (reason.isEmpty()) {
          kickMessage = Message.get("kick.player.noReason");
        } else {
          kickMessage = Message.get("kick.player.reason").set("reason", reason);
        }

        kickMessage
                .set("displayName", player.getDisplayName())
                .set("player", player.getName())
                .set("actor", actor.getName());

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            player.kickPlayer(kickMessage.toString());

            Message message = Message.get(reason.isEmpty() ? "kick.notify.noReason" : "kick.notify.reason");
            message.set("player", player.getName()).set("actor", actor.getName()).set("reason", reason);

            if (isSilent || !sender.hasPermission("bm.notify.kick")) {
              message.sendTo(sender);
            }

            if (!isSilent) CommandUtils.broadcast(message.toString(), "bm.notify.kick");
          }
        });

      }
    });

    return true;
  }

}
