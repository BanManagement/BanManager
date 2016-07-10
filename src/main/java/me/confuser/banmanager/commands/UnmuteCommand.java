package me.confuser.banmanager.commands;

import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
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

public class UnmuteCommand extends BukkitCommand<BanManager> implements TabCompleter {

  public UnmuteCommand() {
    super("unmute");
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
    boolean isMuted;

    if (isUUID) {
      try {
        isMuted = plugin.getPlayerMuteStorage().isMuted(UUID.fromString(playerName));
      } catch (IllegalArgumentException e) {
        sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString());
        return true;
      }
    } else {
      isMuted = plugin.getPlayerMuteStorage().isMuted(playerName);
    }

    if (!isMuted) {
      Message message = Message.get("unmute.error.noExists");
      message.set("player", playerName);

      sender.sendMessage(message.toString());
      return true;
    }

    final String reason = args.length > 1 ? CommandUtils.getReason(1, args).getMessage() : "";

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        final PlayerMuteData mute;

        if (isUUID) {
          mute = plugin.getPlayerMuteStorage().getMute(UUID.fromString(playerName));
        } else {
          mute = plugin.getPlayerMuteStorage().getMute(playerName);
        }

        if (mute == null) {
          sender.sendMessage(Message.get("sender.error.notFound").set("player", playerName).toString
                  ());
          return;
        }

        final PlayerData actor = CommandUtils.getActor(sender);

        //TODO refactor if async perm check is problem
        if (!actor.getUUID().equals(mute.getActor().getUUID()) && !sender.hasPermission("bm.exempt.override.mute")
                && sender.hasPermission("bm.command.unmute.own")) {
          Message.get("unmute.error.notOwn").set("player", mute.getPlayer().getName()).sendTo(sender);
          return;
        }

        boolean unmuted;

        try {
          unmuted = plugin.getPlayerMuteStorage().unmute(mute, actor, reason);
        } catch (SQLException e) {
          sender.sendMessage(Message.get("sender.error.exception").toString());
          e.printStackTrace();
          return;
        }

        if (!unmuted) {
          return;
        }

        Message message = Message.get("unmute.notify");
        message
                .set("player", mute.getPlayer().getName())
                .set("playerId", mute.getPlayer().getUUID().toString())
                .set("actor", actor.getName())
                .set("reason", reason);

        if (!sender.hasPermission("bm.notify.unmute")) {
          message.sendTo(sender);
        }

        CommandUtils.broadcast(message.toString(), "bm.notify.unmute");

        plugin.getServer().getScheduler().runTask(plugin, new Runnable() {

          @Override
          public void run() {
            Player bukkitPlayer = plugin.getServer().getPlayer(mute.getPlayer().getUUID());

            if (bukkitPlayer == null) return;
            if (bukkitPlayer.hasPermission("bm.notify.unmute")) return;

            Message.get("unmute.player")
                    .set("displayName", bukkitPlayer.getDisplayName())
                    .set("player", mute.getPlayer().getName())
                    .set("playerId", mute.getPlayer().getUUID().toString())
                    .set("reason", mute.getReason())
                    .set("actor", actor.getName())
                    .sendTo(bukkitPlayer);

          }
        });
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

    for (PlayerMuteData ban : plugin.getPlayerMuteStorage().getMutes().values()) {
      if (ban.getPlayer().getName().toLowerCase().startsWith(nameSearch)) {
        mostLike.add(ban.getPlayer().getName());
      }
    }

    return mostLike;
  }
}
