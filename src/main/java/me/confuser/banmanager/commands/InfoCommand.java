package me.confuser.banmanager.commands;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.bukkitutil.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class InfoCommand extends AutoCompleteNameTabCommand<BanManager> {

  public InfoCommand() {
    super("bminfo");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    if (args.length > 1) {
      return false;
    }

    if (args.length == 0 && !(sender instanceof Player)) {
      return false;
    }

    if (args.length == 1 && !sender.hasPermission("bm.command.bminfo.others")) {
      Message.get("sender.error.noPermission").sendTo(sender);
      return true;
    }

    final String search = args.length == 1 ? args[0] : sender.getName();
    final boolean isName = !InetAddresses.isInetAddress(search);

    if (isName && search.length() > 16) {
      sender.sendMessage(Message.getString("sender.error.invalidIp"));
      return true;
    }

    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {

      @Override
      public void run() {
        if (isName) {
          try {
            playerInfo(sender, search);
          } catch (SQLException e) {
            sender.sendMessage(Message.getString("sender.error.exception"));
            e.printStackTrace();
            return;
          }
        }/* else {
                         TODO
                         ipInfo(sender, search);
                         }*/

      }

    });

    return true;
  }

  public void playerInfo(CommandSender sender, String name) throws SQLException {
    PlayerData player = plugin.getPlayerStorage().retrieve(name, false);

    if (player == null) {
      sender.sendMessage(Message.get("sender.error.notFound").set("player", name).toString());
      return;
    }

    ArrayList<String> messages = new ArrayList<>();

    if (sender.hasPermission("bm.command.bminfo.playerstats")) {
      long banTotal = plugin.getPlayerBanRecordStorage().getCount(player);
      long muteTotal = plugin.getPlayerMuteRecordStorage().getCount(player);
      long warnTotal = plugin.getPlayerWarnStorage().getCount(player);

      messages.add(Message.get("info.stats.player")
                          .set("player", player.getName())
                          .set("bans", Long.toString(banTotal))
                          .set("mutes", Long.toString(muteTotal))
                          .set("warns", Long.toString(warnTotal)).toString());
    }

    if (sender.hasPermission("bm.command.bminfo.connection")) {
      messages.add(Message.get("info.connection")
                          .set("ip", IPUtils.toString(player.getIp()))
                          .set("lastSeen", new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
                                  .format(new java.util.Date(player.getLastSeen() * 1000L)))
                          .toString());
    }

    if (sender.hasPermission("bm.command.bminfo.alts")) {
      messages.add(Message.getString("alts.header"));

      StringBuilder duplicates = new StringBuilder();

      for (PlayerData duplicatePlayer : plugin.getPlayerStorage().getDuplicates(player.getIp())) {
        duplicates.append(duplicatePlayer.getName() + ", ");
      }

      if (duplicates.length() >= 2) duplicates.setLength(duplicates.length() - 2);

      messages.add(duplicates.toString());
    }

    if (sender.hasPermission("bm.command.bminfo.ipstats")) {

      long ipBanTotal = plugin.getIpBanRecordStorage().getCount(player.getIp());

      messages.add(Message.get("info.stats.ip")
                          .set("bans", Long.toString(ipBanTotal))
                          .toString());
    }

    if (plugin.getPlayerBanStorage().isBanned(player.getUUID())) {
      PlayerBanData ban = plugin.getPlayerBanStorage().getBan(player.getUUID());

      Message message;

      if (ban.getExpires() == 0) {
        message = Message.get("info.ban.permanent");
      } else {
        message = Message.get("info.ban.temporary");
        message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
      }

      String dateTimeFormat = Message.getString("info.ban.dateTimeFormat");

      messages.add(message
              .set("player", player.getName())
              .set("reason", ban.getReason())
              .set("actor", ban.getActor().getName())
              .set("created", new SimpleDateFormat(dateTimeFormat)
                      .format(new java.util.Date(ban.getCreated() * 1000L)))
              .toString());
    }

    if (plugin.getPlayerMuteStorage().isMuted(player.getUUID())) {
      PlayerMuteData mute = plugin.getPlayerMuteStorage().getMute(player.getUUID());

      Message message;

      if (mute.getExpires() == 0) {
        message = Message.get("info.mute.permanent");
      } else {
        message = Message.get("info.mute.temporary");
        message.set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
      }

      String dateTimeFormat = Message.getString("info.mute.dateTimeFormat");

      messages.add(message
              .set("player", player.getName())
              .set("reason", mute.getReason())
              .set("actor", mute.getActor().getName())
              .set("created", new SimpleDateFormat(dateTimeFormat)
                      .format(new java.util.Date(mute.getCreated() * 1000L)))
              .toString());
    }

    if (sender.hasPermission("bm.command.bminfo.website")) {
      messages.add(Message.get("info.website.player")
                          .set("player", player.getName())
                          .set("uuid", player.getUUID().toString())
                          .toString());
    }

    // TODO Show last warning
    for (String message : messages) {
      sender.sendMessage(message);
    }
  }
}
