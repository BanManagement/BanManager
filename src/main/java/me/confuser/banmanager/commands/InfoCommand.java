package me.confuser.banmanager.commands;

import com.google.common.net.InetAddresses;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.data.PlayerBanData;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerMuteData;
import me.confuser.banmanager.util.DateUtils;
import me.confuser.banmanager.util.IPUtils;
import me.confuser.banmanager.util.parsers.InfoCommandParser;
import me.confuser.bukkitutil.Message;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class InfoCommand extends AutoCompleteNameTabCommand<BanManager> {

  private static final FastDateFormat LAST_SEEN_COMMAND_FORMAT = FastDateFormat.getInstance("dd-MM-yyyy HH:mm:ss");

  public InfoCommand() {
    super("bminfo");
  }

  @Override
  public boolean onCommand(final CommandSender sender, Command command, String commandName, String[] args) {
    final InfoCommandParser parser;

    try {
      parser = new InfoCommandParser(args);
    } catch (IllegalArgumentException e) {
      sender.sendMessage(Message.getString("info.error.incorrectFlagUsage"));
      return true;
    }

    args = parser.getArgs();

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
            playerInfo(sender, search, parser);
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

  public void playerInfo(CommandSender sender, String name, InfoCommandParser parser) throws SQLException {
    PlayerData player = plugin.getPlayerStorage().retrieve(name, false);

    if (player == null) {
      sender.sendMessage(Message.get("sender.error.notFound").set("player", name).toString());
      return;
    }

    ArrayList<String> messages = new ArrayList<>();

    boolean hasFlags = parser.isBans() || parser.isKicks() || parser.isMutes() || parser.isNotes() || parser
            .isWarnings();

    if (hasFlags) {
      long since = 0;

      if (parser.getTime() != null && !parser.getTime().isEmpty()) {
        try {
          since = DateUtils.parseDateDiff(parser.getTime(), false);
        } catch (Exception e1) {
          sender.sendMessage(Message.get("time.error.invalid").toString());
          return;
        }
      }
      if (parser.isBans() && !sender.hasPermission("bm.command.bminfo.history.bans")) {
        Message.get("sender.error.noPermission").sendTo(sender);
        return;
      }

      if (parser.isKicks() && !sender.hasPermission("bm.command.bminfo.history.kicks")) {
        Message.get("sender.error.noPermission").sendTo(sender);
        return;
      }

      if (parser.isMutes() && !sender.hasPermission("bm.command.bminfo.history.mutes")) {
        Message.get("sender.error.noPermission").sendTo(sender);
        return;
      }

      if (parser.isNotes() && !sender.hasPermission("bm.command.bminfo.history.notes")) {
        Message.get("sender.error.noPermission").sendTo(sender);
        return;
      }

      if (parser.isWarnings() && !sender.hasPermission("bm.command.bminfo.history.warnings")) {
        Message.get("sender.error.noPermission").sendTo(sender);
        return;
      }

      ArrayList<HashMap<String, Object>> results;

      if (parser.getTime() != null && !parser.getTime().isEmpty()) {
        results = plugin.getHistoryStorage().getSince(player, since, parser);
      } else {
        results = plugin.getHistoryStorage().getAll(player, parser);
      }

      if (results == null || results.size() == 0) {
        Message.get("info.history.noResults").sendTo(sender);
        return;
      }

      String dateTimeFormat = Message.getString("info.history.dateTimeFormat");
      FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

      for (HashMap<String, Object> result : results) {
        Message message = Message.get("info.history.row")
                                 .set("id", (int) result.get("id"))
                                 .set("reason", (String) result.get("reason"))
                                 .set("type", (String) result.get("type"))
                                 .set("created", dateFormatter
                                         .format((long) result.get("created") * 1000L))
                                 .set("actor", (String) result.get("actor"));

        messages.add(message.toString());
      }

    } else {

      if (sender.hasPermission("bm.command.bminfo.playerstats")) {
        long banTotal = plugin.getPlayerBanRecordStorage().getCount(player);
        long muteTotal = plugin.getPlayerMuteRecordStorage().getCount(player);
        long warnTotal = plugin.getPlayerWarnStorage().getCount(player);
        long kickTotal = plugin.getPlayerKickStorage().getCount(player);

        messages.add(Message.get("info.stats.player")
                            .set("player", player.getName())
                            .set("bans", Long.toString(banTotal))
                            .set("mutes", Long.toString(muteTotal))
                            .set("warns", Long.toString(warnTotal))
                            .set("kicks", Long.toString(kickTotal))
                            .toString());
      }

      if (sender.hasPermission("bm.command.bminfo.connection")) {
        messages.add(Message.get("info.connection")
                .set("ip", IPUtils.toString(player.getIp()))
                .set("lastSeen", LAST_SEEN_COMMAND_FORMAT
                        .format(player.getLastSeen() * 1000L))
                .toString());
      }

      if (plugin.getGeoIpConfig().isEnabled() && sender.hasPermission("bm.command.bminfo.geoip")) {
        Message message = Message.get("info.geoip");

        try {
          InetAddress ip = IPUtils.toInetAddress(player.getIp());

          CountryResponse countryResponse = plugin.getGeoIpConfig().getCountryDatabase().country(ip);
          String country = countryResponse.getCountry().getName();
          String countryIso = countryResponse.getCountry().getIsoCode();

          CityResponse cityResponse = plugin.getGeoIpConfig().getCityDatabase().city(ip);
          String city = cityResponse.getCity().getName();

          message.set("country", country)
                 .set("countryIso", countryIso)
                 .set("city", city);
          city = cityResponse.getCity().getName();
        } catch (IOException | GeoIp2Exception ignored) {
        }

      }

      if (sender.hasPermission("bm.command.bminfo.alts")) {
        messages.add(Message.getString("alts.header"));

        StringBuilder duplicates = new StringBuilder();

        for (PlayerData duplicatePlayer : plugin.getPlayerStorage().getDuplicates(player.getIp())) {
          duplicates.append(duplicatePlayer.getName()).append(", ");
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
                .set("created", FastDateFormat.getInstance(dateTimeFormat)
                        .format(ban.getCreated() * 1000L))
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
                .set("created", FastDateFormat.getInstance(dateTimeFormat)
                        .format(mute.getCreated() * 1000L))
                .toString());
      }

      if (sender.hasPermission("bm.command.bminfo.website")) {
        messages.add(Message.get("info.website.player")
                            .set("player", player.getName())
                            .set("uuid", player.getUUID().toString())
                            .toString());
      }
    }

    // TODO Show last warning
    for (String message : messages) {
      sender.sendMessage(message);
    }
  }
}
