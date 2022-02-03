package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.data.*;
import me.confuser.banmanager.common.gson.JsonElement;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.TextComponent;
import me.confuser.banmanager.common.kyori.text.event.ClickEvent;
import me.confuser.banmanager.common.maxmind.db.model.CountryResponse;
import me.confuser.banmanager.common.ormlite.dao.CloseableIterator;
import me.confuser.banmanager.common.util.DateUtils;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.StringUtils;
import me.confuser.banmanager.common.util.parsers.InfoCommandParser;

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
    final boolean isValidName = StringUtils.isValidPlayerName(search);

    if (!isValidName && !IPUtils.isValid(search)) {
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
      try {
        if (isValidName) {
          playerInfo(sender, search, index, parser);
        } else {
          ipInfo(sender, IPUtils.toIPAddress(search), parser);
        }
      } catch (SQLException e) {
        sender.sendMessage(Message.getString("sender.error.exception"));
        e.printStackTrace();
        return;
      }
    });

    return true;
  }

  public void ipInfo(CommonSender sender, IPAddress ip, InfoCommandParser parser) throws
      SQLException {
    ArrayList<Object> messages = new ArrayList<>();

    boolean hasFlags = parser.isBans() || parser.isMutes();

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

      if (parser.isBans() && !sender.hasPermission("bm.command.bminfo.history.ipbans")) {
        Message.get("sender.error.noPermission").sendTo(sender);
        return;
      }

      if (parser.isMutes() && !sender.hasPermission("bm.command.bminfo.history.ipmutes")) {
        Message.get("sender.error.noPermission").sendTo(sender);
        return;
      }

      ArrayList<HashMap<String, Object>> results;

      if (parser.getTime() != null && !parser.getTime().isEmpty()) {
        results = getPlugin().getHistoryStorage().getSince(ip, since, parser);
      } else {
        results = getPlugin().getHistoryStorage().getAll(ip, parser);
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
    } else {
      if (sender.hasPermission("bm.command.bminfo.ipstats")) {

        long ipBanTotal = getPlugin().getIpBanRecordStorage().getCount(ip);
        long ipMuteTotal = getPlugin().getIpMuteRecordStorage().getCount(ip);
        long ipRangeBanTotal = getPlugin().getIpRangeBanRecordStorage().getCount(ip);

        messages.add(Message.get("info.stats.ip")
            .set("bans", Long.toString(ipBanTotal))
            .set("mutes", Long.toString(ipMuteTotal))
            .set("rangebans", Long.toString(ipRangeBanTotal))
            .toString());
      }

      if (getPlugin().getGeoIpConfig().isEnabled() && sender.hasPermission("bm.command.bminfo.geoip")) {
        try {
          CountryResponse countryResponse = getPlugin().getGeoIpConfig().getCountryDatabase().getCountry(ip.toInetAddress());

          if (countryResponse != null) {
            String country = countryResponse.getCountry().getName();
            String countryIso = countryResponse.getCountry().getIsoCode();
            String city = "";
            JsonElement cityResponse = getPlugin().getGeoIpConfig().getCityDatabase().get(ip.toInetAddress());

            if (cityResponse != null && !cityResponse.isJsonNull()) {
              city = cityResponse.getAsJsonObject().get("city").getAsJsonObject().get("names").getAsJsonObject().get("en").getAsString();
            }

            Message message = Message.get("info.geoip");

            message.set("country", country)
                .set("countryIso", countryIso)
                .set("city", city);
            messages.add(message.toString());
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      if (sender.hasPermission("bm.command.bminfo.alts")) {
        messages.add(Message.getString("alts.header"));

        List<PlayerData> duplicatePlayers = getPlugin().getPlayerStorage().getDuplicatesInTime(ip, getPlugin().getConfig().getTimeAssociatedAlts());

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

      if (getPlugin().getIpBanStorage().isBanned(ip)) {
        IpBanData ban = getPlugin().getIpBanStorage().getBan(ip);

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
            .set("id", ban.getId())
            .set("created", DateUtils.format(dateTimeFormat, ban.getCreated()))
            .toString());
      }

      if (getPlugin().getIpMuteStorage().isMuted(ip)) {
        IpMuteData mute = getPlugin().getIpMuteStorage().getMute(ip);

        Message message;

        if (mute.getExpires() == 0) {
          message = Message.get("info.ipmute.permanent");
        } else {
          message = Message.get("info.ipmute.temporary");
          message.set("expires", DateUtils.getDifferenceFormat(mute.getExpires()));
        }

        String dateTimeFormat = Message.getString("info.ipmute.dateTimeFormat");

        messages.add(message
            .set("reason", mute.getReason())
            .set("actor", mute.getActor().getName())
            .set("id", mute.getId())
            .set("created", DateUtils.format(dateTimeFormat, mute.getCreated()))
            .toString());
      }

      if (getPlugin().getIpRangeBanStorage().isBanned(ip)) {
        IpRangeBanData ban = getPlugin().getIpRangeBanStorage().getBan(ip);

        Message message;

        if (ban.getExpires() == 0) {
          message = Message.get("info.iprangeban.permanent");
        } else {
          message = Message.get("info.iprangeban.temporary");
          message.set("expires", DateUtils.getDifferenceFormat(ban.getExpires()));
        }

        String dateTimeFormat = Message.getString("info.iprangeban.dateTimeFormat");

        messages.add(message
            .set("reason", ban.getReason())
            .set("actor", ban.getActor().getName())
            .set("id", ban.getId())
            .set("created", DateUtils.format(dateTimeFormat, ban.getCreated()))
            .set("from", ban.getFromIp().toString())
            .set("to", ban.getToIp().toString())
            .toString());
      }
    }

    for (Object message : messages) {
      if (message instanceof String) {
        sender.sendMessage((String) message);
      } else if (message instanceof TextComponent) {
        ((CommonPlayer) sender).sendJSONMessage((TextComponent) message);
      } else {
        getPlugin().getLogger().warning("Invalid info message, please report the following as a bug: " + message.toString());
      }
    }
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
        long noteTotal = getPlugin().getPlayerNoteStorage().getCount(player);

        messages.add(Message.get("info.stats.player")
            .set("player", player.getName())
            .set("playerId", player.getUUID().toString())
            .set("bans", Long.toString(banTotal))
            .set("notes", Long.toString(noteTotal))
            .set("mutes", Long.toString(muteTotal))
            .set("warns", Long.toString(warnTotal))
            .set("warnPoints", warnPointsTotal)
            .set("kicks", Long.toString(kickTotal))
            .toString());
      }

      if (sender.hasPermission("bm.command.bminfo.connection")) {
        messages.add(Message.get("info.connection")
            .set("player", player.getName())
            .set("ip", player.getIp().toString())
            .set("lastSeen", DateUtils.format("dd-MM-yyyy HH:mm:ss", player.getLastSeen()))
            .toString());
      }

      if (getPlugin().getGeoIpConfig().isEnabled() && sender.hasPermission("bm.command.bminfo.geoip")) {

        try {
          InetAddress ip = player.getIp().toInetAddress();
          CountryResponse countryResponse = getPlugin().getGeoIpConfig().getCountryDatabase().getCountry(ip);

          if (countryResponse != null) {
            String country = countryResponse.getCountry().getName();
            String countryIso = countryResponse.getCountry().getIsoCode();
            String city = "";
            JsonElement cityResponse = getPlugin().getGeoIpConfig().getCityDatabase().get(ip);

            if (cityResponse != null && !cityResponse.isJsonNull()) {
              city = cityResponse.getAsJsonObject().get("city").getAsJsonObject().get("names").getAsJsonObject().get("en").getAsString();
            }

            Message message = Message.get("info.geoip");

            message.set("country", country)
                .set("countryIso", countryIso)
                .set("city", city);
            messages.add(message.toString());
          }
        } catch (IOException e) {
          e.printStackTrace();
        }

      }

      if (sender.hasPermission("bm.command.bminfo.alts")) {
        messages.add(Message.getString("alts.header"));

        List<PlayerData> duplicatePlayers = getPlugin().getPlayerStorage().getDuplicatesInTime(player.getIp(), getPlugin().getConfig().getTimeAssociatedAlts());

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
        long ipMuteTotal = getPlugin().getIpMuteRecordStorage().getCount(player.getIp());
        long ipRangeBanTotal = getPlugin().getIpRangeBanRecordStorage().getCount(player.getIp());

        messages.add(Message.get("info.stats.ip")
            .set("bans", Long.toString(ipBanTotal))
            .set("mutes", Long.toString(ipMuteTotal))
            .set("rangebans", Long.toString(ipRangeBanTotal))
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
              .set("id", ban.getId())
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
            .set("id", ban.getId())
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
            .set("id", mute.getId())
            .toString());
      }

      if (sender.hasPermission("bm.command.bminfo.website")) {
        String websiteMsg = Message.get("info.website.player")
            .set("player", player.getName())
            .set("uuid", player.getUUID().toString())
            .set("playerId", player.getUUID().toString())
            .toString();

        if (sender.isConsole()) {
          messages.add(websiteMsg);
        } else {
          TextComponent.Builder message = Component.text();
          message
                  .append(
                          Component.text(websiteMsg)
                                  .clickEvent(ClickEvent.openUrl(websiteMsg)));
          messages.add(message.build());
        }
      }
    }

    // TODO Show last warning
    for (Object message : messages) {
      if (message instanceof String) {
        sender.sendMessage((String) message);
      } else if (message instanceof TextComponent) {
        ((CommonPlayer) sender).sendJSONMessage((TextComponent) message);
        getPlugin().getLogger().info("debugging bminfo message");
      } else {
        getPlugin().getLogger().warning("Invalid info message, please report the following as a bug: " + message.toString());
      }
    }
  }

  private void handleIpHistory(ArrayList<Object> messages, PlayerData player, long since, int page) {
    CloseableIterator<PlayerHistoryData> iterator = null;
    try {
      iterator = getPlugin().getPlayerHistoryStorage().getSince(player, since, page);

      String dateTimeFormat = Message.getString("info.ips.dateTimeFormat");

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
