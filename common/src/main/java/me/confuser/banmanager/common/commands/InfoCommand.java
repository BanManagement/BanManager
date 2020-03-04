package me.confuser.banmanager.common.commands;

import com.j256.ormlite.dao.CloseableIterator;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.model.CountryResponse;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.parsers.InfoCommandParser;
import net.kyori.text.TextComponent;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InfoCommand extends CommonCommand {

  public InfoCommand(BanManagerPlugin plugin) {
    super(plugin, "bminfo", true, InfoCommandParser.class, 0);
  }

  @Override
  public boolean onCommand(final CommonSender sender, CommandParser originalParser) {
    final InfoCommandParser parser = (InfoCommandParser) originalParser;

    if (parser.args.length > 2) {
      return false;
    }

    if (parser.args.length == 0 && sender.isConsole()) {
      return false;
    }

    if (parser.args.length >= 1 && !sender.hasPermission("bm.command.bminfo.others")) {
      Message.get("sender.error.noPermission").sendTo(sender);
      return true;
    }

    final String search = parser.args.length > 0 ? parser.args[0] : sender.getName();
    final boolean isName = !IPUtils.isValid(search);

    if (isName && search.length() > 16) {
      sender.sendMessage(Message.getString("sender.error.invalidIp"));
      return true;
    }

    final Integer index;

    try {
      index = parser.args.length == 2 ? Integer.parseInt(parser.args[1]) : null;
    } catch (NumberFormatException e) {
      Message.get("info.error.invalidIndex").sendTo(sender);
      return true;
    }

    getPlugin().getScheduler().runAsync(() -> {
      if (isName) {
        try {
          playerInfo(sender, search, index, parser);
        } catch (SQLException e) {
          sender.sendMessage(Message.getString("sender.error.exception"));
          e.printStackTrace();
          return;
        }
      }/* else {
                       TODO
                       ipInfo(sender, search);
                       }*/

    });

    return true;
  }

  public void playerInfo(CommonSender sender, String name, Integer index, InfoCommandParser parser) throws
      SQLException {
    List<PlayerData> players = getPlugin().getPlayerStorage().retrieve(name);

    if (players == null || players.size() == 0) {
      sender.sendMessage(Message.get("sender.error.notFound").set("player", name).toString());
      return;
    }

    if (players.size() > 1 && (index == null || index > players.size() || index < 1)) {
      Message.get("info.error.indexRequired")
          .set("size", players.size())
          .set("name", name)
          .sendTo(sender);

      int i = 0;
      for (PlayerData player : players) {
        i++;

        Message.get("info.error.index")
            .set("index", i)
            .set("uuid", player.getUUID().toString())
            .set("name", player.getName())
            .sendTo(sender);
      }

      return;
    }

    if (players.size() == 1) index = 1;

    PlayerData player = players.get(index - 1);

    ArrayList<Object> messages = new ArrayList<>();

    boolean hasFlags = parser.isBans() || parser.isKicks() || parser.isMutes() || parser.isNotes() || parser
        .isWarnings() || parser.getIps() != null;

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

      if (parser.getIps() != null) {
        if (!sender.hasPermission("bm.command.bminfo.history.ips")) {
          Message.get("sender.error.noPermission").sendTo(sender);
          return;
        }

        int page = parser.getIps() - 1;

        if (page < 0) page = 0;

        handleIpHistory(messages, player, since, page);
      } else {

        ArrayList<HashMap<String, Object>> results;

        if (parser.getTime() != null && !parser.getTime().isEmpty()) {
          results = getPlugin().getHistoryStorage().getSince(player, since, parser);
        } else {
          results = getPlugin().getHistoryStorage().getAll(player, parser);
        }

        if (results == null || results.size() == 0) {
          Message.get("info.history.noResults").sendTo(sender);
          return;
        }

        String dateTimeFormat = Message.getString("info.history.dateTimeFormat");

        for (HashMap<String, Object> result : results) {
          Message message = Message.get("info.history.row")
              .set("id", (int) result.get("id"))
              .set("reason", (String) result.get("reason"))
              .set("type", (String) result.get("type"))
              .set("created", DateUtils
                  .format(dateTimeFormat, (long) result.get("created")))
              .set("actor", (String) result.get("actor"))
              .set("meta", (String) result.get("meta"));

          messages.add(message.toString());
        }
      }

    } else {

      if (sender.hasPermission("bm.command.bminfo.playerstats")) {
        long banTotal = getPlugin().getPlayerBanRecordStorage().getCount(player);
        long muteTotal = getPlugin().getPlayerMuteRecordStorage().getCount(player);
        long warnTotal = getPlugin().getPlayerWarnStorage().getCount(player);
        double warnPointsTotal = getPlugin().getPlayerWarnStorage().getPointsCount(player);
        long kickTotal = getPlugin().getPlayerKickStorage().getCount(player);

        messages.add(Message.get("info.stats.player")
            .set("player", player.getName())
            .set("playerId", player.getUUID().toString())
            .set("bans", Long.toString(banTotal))
            .set("mutes", Long.toString(muteTotal))
            .set("warns", Long.toString(warnTotal))
            .set("warnPoints", warnPointsTotal)
            .set("kicks", Long.toString(kickTotal))
            .toString());
      }

      if (sender.hasPermission("bm.command.bminfo.connection")) {
        messages.add(Message.get("info.connection")
            .set("ip", player.getIp().toString())
            .set("lastSeen", DateUtils.format("dd-MM-yyyy HH:mm:ss", player.getLastSeen()))
            .toString());
      }

      if (getPlugin().getGeoIpConfig().isEnabled() && sender.hasPermission("bm.command.bminfo.geoip")) {
        Message message = Message.get("info.geoip");

        try {
          InetAddress ip = player.getIp().toInetAddress();

          CountryResponse countryResponse = getPlugin().getGeoIpConfig().getCountryDatabase().country(ip);
          String country = countryResponse.getCountry().getName();
          String countryIso = countryResponse.getCountry().getIsoCode();

          CityResponse cityResponse = getPlugin().getGeoIpConfig().getCityDatabase().city(ip);
          String city = cityResponse.getCity().getName();

          message.set("country", country)
              .set("countryIso", countryIso)
              .set("city", city);
          messages.add(message);
        } catch (IOException | GeoIp2Exception ignored) {
        }

      }

      if (sender.hasPermission("bm.command.bminfo.alts")) {
        messages.add(Message.getString("alts.header"));

        List<PlayerData> duplicatePlayers = getPlugin().getPlayerStorage().getDuplicates(player.getIp());

        if (!sender.isConsole()) {
          messages.add(FindAltsCommand.alts(duplicatePlayers));
        } else {
          StringBuilder duplicates = new StringBuilder();

          for (PlayerData duplicatePlayer : duplicatePlayers) {
            duplicates.append(duplicatePlayer.getName()).append(", ");
          }

          if (duplicates.length() >= 2) duplicates.setLength(duplicates.length() - 2);

          messages.add(duplicates.toString());
        }
      }

      if (sender.hasPermission("bm.command.bminfo.ipstats")) {

        long ipBanTotal = getPlugin().getIpBanRecordStorage().getCount(player.getIp());

        messages.add(Message.get("info.stats.ip")
            .set("bans", Long.toString(ipBanTotal))
            .toString());

        if (getPlugin().getIpBanStorage().isBanned(player.getIp())) {
          IpBanData ban = getPlugin().getIpBanStorage().getBan(player.getIp());

          Message message;

          if (ban.getExpires() == 0) {
            message = Message.get("info.ipban.permanent");
          } else {
            message = Message.get("info.ipban.temporary");
            message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
          }

          String dateTimeFormat = Message.getString("info.ipban.dateTimeFormat");

          messages.add(message
              .set("reason", ban.getReason())
              .set("actor", ban.getActor().getName())
              .set("created", DateUtils.format(dateTimeFormat, ban.getCreated()))
              .toString());
        }
      }

      if (getPlugin().getPlayerBanStorage().isBanned(player.getUUID())) {
        PlayerBanData ban = getPlugin().getPlayerBanStorage().getBan(player.getUUID());

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
            .set("created", DateUtils.format(dateTimeFormat, ban.getCreated()))
            .toString());
      }

      if (getPlugin().getPlayerMuteStorage().isMuted(player.getUUID())) {
        PlayerMuteData mute = getPlugin().getPlayerMuteStorage().getMute(player.getUUID());

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
            .set("created", DateUtils.format(dateTimeFormat, mute.getCreated()))
            .toString());
      }

      if (sender.hasPermission("bm.command.bminfo.website")) {
        messages.add(Message.get("info.website.player")
            .set("player", player.getName())
            .set("uuid", player.getUUID().toString())
            .set("playerId", player.getUUID().toString())
            .toString());
      }
    }

    // TODO Show last warning
    for (Object message : messages) {
      if (message instanceof String) {
        sender.sendMessage((String) message);
      } else if (message instanceof TextComponent) {
        ((CommonPlayer) sender).sendJSONMessage((TextComponent) message);
      }
    }
  }

  private void handleIpHistory(ArrayList<Object> messages, PlayerData player, long since, int page) {
    CloseableIterator<PlayerHistoryData> iterator = null;
    try {
      iterator = getPlugin().getPlayerHistoryStorage().getSince(player, since, page);

      String dateTimeFormat = Message.getString("info.mute.dateTimeFormat");

      while (iterator.hasNext()) {
        PlayerHistoryData data = iterator.next();

        messages.add(Message.get("info.ips.row")
            .set("join", DateUtils.format(dateTimeFormat, data.getJoin()))
            .set("leave", DateUtils.format(dateTimeFormat, data.getLeave()))
            .set("ip", data.getIp().toString())
            .toString());
      }
    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      if (iterator != null) iterator.closeQuietly();
    }
  }
}
