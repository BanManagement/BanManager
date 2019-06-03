package me.confuser.banmanager.util;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.commands.report.ReportList;
import me.confuser.banmanager.common.locale.message.Message;
import me.confuser.banmanager.common.plugin.BanManagerPlugin;
import me.confuser.banmanager.common.sender.Sender;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.parsers.Reason;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class CommandUtils {

  private static BanManagerPlugin plugin = BanManager.getPlugin();

  public static void dispatchCommand(Sender sender, String command) {
    Bukkit.dispatchCommand(sender, command);
  }

  public static void dispatchCommands(Sender sender, List<String> commands) {
    for (String command : commands) {
      dispatchCommand(sender, command);
    }
  }

  public static void handleMultipleNames(Sender sender, String commandName, String[] args) {
    String[] names = splitNameDelimiter(args[0]);
    String argsStr = StringUtils.join(args, " ", 1, args.length);
    ArrayList<String> commands = new ArrayList<>(names.length);

    for (String name : names) {
      if (name.length() == 0) continue;
      commands.add(commandName + " " + name + " " + argsStr);
    }

    dispatchCommands(sender, commands);
  }

  public static void handleMultipleNames(Sender sender, String commandName, List<String> args) {
    String[] names = splitNameDelimiter(args.get(0));
    String argsStr = StringUtils.join(args, " ", 1, args.size());
    ArrayList<String> commands = new ArrayList<>(names.length);

    for (String name : names) {
      if (name.length() == 0) continue;
      commands.add(commandName + " " + name + " " + argsStr);
    }

    dispatchCommands(sender, commands);
  }

  public static boolean isValidNameDelimiter(String names) {
    return names.contains("|") || names.contains(",");
  }

  public static String[] splitNameDelimiter(String str) {
    String delimiter;

    if (str.contains("|")) {
      delimiter = "\\|";
    } else {
      delimiter = ",";
    }

    return str.split(delimiter);
  }

  public static void broadcast(String s, String permission) {
    //TODO
  }

  public static void broadcast(JSONMessage message, String permission) {
    Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions("bukkit.broadcast.user");
    for (Permissible permissible : permissibles) {
      if (permissible instanceof Player && permissible.hasPermission(permission)) {
        message.send((Player) permissible);
      }
    }
  }

  public static void broadcast(String message, String permission, Sender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
  }

  @Deprecated
  public static Reason getReason(int start, String[] args) {
    String reason = StringUtils.join(args, " ", start, args.length);
    List<String> notes = new ArrayList<>();

    String[] matches = null;
    if (plugin.getConfiguration().isCreateNoteReasons()) {
      matches = StringUtils.substringsBetween(reason, "(", ")");
    }

    if (matches != null) notes = Arrays.asList(matches);

    for (int i = start; i < args.length; i++) {
      if (!args[i].startsWith("#")) continue;

      String key = args[i].replace("#", "");
      String replace = BanManager.getPlugin().getReasonsConfig().getReason(key);

      if (replace != null) reason = reason.replace("#" + key, replace);
    }

    for (String note : notes) {
      reason = reason.replace("(" + note + ")", "");
    }

    reason = reason.trim();

    return new Reason(reason, notes);
  }

  public static Reason getReason(int start, List<String> args) {
    String reason = StringUtils.join(args, " ", start, args.size());
    List<String> notes = new ArrayList<>();

    String[] matches = null;
    if (plugin.getConfiguration().isCreateNoteReasons()) {
      matches = StringUtils.substringsBetween(reason, "(", ")");
    }

    if (matches != null) notes = Arrays.asList(matches);

    for (int i = start; i < args.size(); i++) {
      if (!args.get(i).startsWith("#")) continue;

      String key = args.get(i).replace("#", "");
      String replace = BanManager.getPlugin().getReasonsConfig().getReason(key);

      if (replace != null) reason = reason.replace("#" + key, replace);
    }

    for (String note : notes) {
      reason = reason.replace("(" + note + ")", "");
    }

    reason = reason.trim();

    return new Reason(reason, notes);
  }

  public static void handlePrivateNotes(PlayerData player, PlayerData actor, Reason reason) {
    if (plugin.getConfiguration().isCreateNoteReasons())
      if (reason.getNotes().size() == 0) return;

    for (String note : reason.getNotes()) {
      try {
        plugin.getPlayerNoteStorage().create(new PlayerNoteData(player, actor, note));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

  }

  public static boolean isUUID(String player) {
    return player.length() > 16;
  }

  public static PlayerData getPlayer(Sender sender, String playerName, boolean mojangLookup) {
    boolean isUUID = isUUID(playerName);
    PlayerData player = null;

    if (isUUID) {
      try {
        player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
      }
    } else {
      player = plugin.getPlayerStorage().retrieve(playerName, mojangLookup);
    }

    return player;
  }

  public static Player getPlayer(UUID uuid) {
    if (plugin.getConfiguration().isOnlineMode()) {
      return plugin.getBootstrap().getPlayer(uuid);
    }

    for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
      if (UUIDUtils.getUUID(onlinePlayer).equals(uuid))
        return onlinePlayer;
    }

    return null;
  }

  public static Sender getSender(UUID uuid) {
    if (plugin.getConfiguration().isOnlineMode()) {
      return plugin.getBootstrap().getPlayerAsSender(uuid).orElse(null);
    }

    for (UUID onlineUUID : plugin.getBootstrap().getOnlinePlayers().collect(Collectors.toList())) {
      Sender sender = plugin.getBootstrap().getPlayerAsSender(onlineUUID).orElse(null);
      if(UUIDUtils.getUUID(sender).equals(uuid))
        return sender;
    }

    return null;
  }

  public static PlayerData getPlayer(Sender sender, String playerName) {
    return getPlayer(sender, playerName, true);
  }

  public static PlayerData getActor(Sender sender) {
    PlayerData actor = null;

    if (!sender.isConsole()) {
      try {
        actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(sender));
      } catch (SQLException e) {
        Message.SENDER_ERROR_EXCEPTION.send(sender);
        e.printStackTrace();
      }
    } else {
      actor = plugin.getPlayerStorage().getConsole();
    }

    return actor;
  }

  public static Long getIp(String ipStr) {
    final boolean isName = !InetAddresses.isInetAddress(ipStr);
    Long ip = null;

    if (isName) {
      PlayerData player = plugin.getPlayerStorage().retrieve(ipStr, false);
      if (player == null) return ip;

      ip = player.getIp();
    } else {
      ip = IPUtils.toLong(ipStr);
    }

    return ip;
  }

  public static void sendReportList(ReportList reports, Sender sender, int page) {
    String dateTimeFormat = Message.REPORT_LIST_ROW_DATETIMEFORMAT.getMessage();
    FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

    Message.REPORT_LIST_ROW_HEADER.send(sender,
            "page", page,
            "maxPage", reports.getMaxPage(),
            "count", reports.getCount());

    for (PlayerReportData report : reports.getList()) {
      String message = Message.get("report.list.row.all")
                              .set("id", report.getId())
                              .set("state", report.getState().getName())
                              .set("player", report.getPlayer().getName())
                              .set("actor", report.getActor().getName())
                              .set("reason", report.getReason())
                              .set("created", dateFormatter
                                      .format(report.getCreated() * 1000L))
                              .set("updated", dateFormatter
                                      .format(report.getUpdated() * 1000L)).toString();

      if (!sender.isConsole()) {
        JSONMessage.create(message).runCommand("/reports info " + report.getId()).send((Player) sender);
      } else {
        sender.sendMessage(message);
      }
    }
  }

  public static void handlePunishmentCreateException(SQLException e, Sender sender, String duplicateMessage) {
    // For some reason ORMLite hides the error code (returns 0 instead of 1062)
    if (e.getCause().getMessage().startsWith("Duplicate entry")) {
      sender.sendMessage(duplicateMessage);
      return;
    }

    Message.SENDER_ERROR_EXCEPTION.send(sender);
    e.printStackTrace();
  }
}
