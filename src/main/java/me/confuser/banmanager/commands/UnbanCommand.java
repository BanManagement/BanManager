package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.util.CommandUtils;
import me.confuser.bukkitutil.Message;
import me.confuser.bukkitutil.commands.BukkitCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UnbanCommand extends BukkitCommand<BanManager> implements TabCompleter {

  public UnbanCommand() {
    super("unban");
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

    // Check if UUID vs name
    final String playerName = args[0];
    final boolean isUUID = playerName.length() > 16;
    boolean isBanned;

    if (isUUID) {
      isBanned = plugin.getPlayerBanStorage().isBanned(UUID.fromString(playerName));
    } else {
      isBanned = plugin.getPlayerBanStorage().isBanned(playerName);
    }

    if (!isBanned) {
      Message message = Message.get("unban.error.noExists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        PlayerBanData ban;

        if (isUUID) {
          ban = plugin.getPlayerBanStorage().getBan(UUID.fromString(playerName));
        } else {
          ban = plugin.getPlayerBanStorage().getBan(playerName);
        }

        if (ban == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
          return;
        }

        PlayerData actor;

        if (sender instanceof Player) {
          actor = plugin.getPlayerStorage().getOnline((Player) sender);
        } else {
          actor = plugin.getPlayerStorage().getConsole();
        }

        //TODO refactor if async perm check is problem
        if (actor.getUUID().equals(ban.getActor().getUUID()) && !sender.hasPermission("bm.exempt.override.ban")
                && sender.hasPermission("bm.command.unban.own")) {
          Message.get("unban.error.notOwn").set("player", ban.getPlayer().getName()).sendTo(sender);
          return;
        }

        boolean unbanned;

        try {
          unbanned = plugin.getPlayerBanStorage().unban(ban, actor);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!unbanned) {
          return;
        }

        Message message = Message.get("unban.notify");
        message
                .set("player", ban.getPlayer().getName())
                .set("actor", actor.getName());

        if (!sender.hasPermission("bm.notify.unban")) {
          message.sendTo(sender);
        }

        plugin.getServer().broadcast(message.toString(), "bm.notify.unban");
      }

    });

    return true;
  }

  @Override
  public List<String> onTabComplete(CommandSender sender, Command command, String commandName, String[] args) {

    ArrayList<String> mostLike = new ArrayList<>();

    if (!sender.hasPermission(command.getPermission())) return mostLike;
    if (args.length != 1) return mostLike;

    String nameSearch = args[0].toLowerCase();

    for (PlayerBanData ban : plugin.getPlayerBanStorage().getBans().values()) {
      if (ban.getPlayer().getName().toLowerCase().startsWith(nameSearch)) {
        mostLike.add(ban.getPlayer().getName());
      }
    }

    return mostLike;
  }
}
