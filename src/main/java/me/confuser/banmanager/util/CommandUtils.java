package me.confuser.banmanager.util;

import com.google.common.net.InetAddresses;
import me.confuser.banmanager.BanManager;
import me.confuser.banmanager.commands.report.ReportList;
import me.confuser.banmanager.data.PlayerData;
import me.confuser.banmanager.data.PlayerNoteData;
import me.confuser.banmanager.data.PlayerReportData;
import me.confuser.banmanager.util.parsers.Reason;
import me.confuser.bukkitutil.Message;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.bukkit.Bukkit;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;

import java.sql.SQLException;
import java.util.*;

public class CommandUtils {

  private static BanManager plugin = BanManager.getPlugin();

  public static void dispatchCommand(CommandSender sender, String command) {
    Bukkit.dispatchCommand(sender, command);
  }

  public static void dispatchCommands(CommandSender sender, List<String> commands) {
    for (String command : commands) {
      dispatchCommand(sender, command);
    }
  }

  public static void handleMultipleNames(CommandSender sender, String commandName, String[] args) {
    String[] names = splitNameDelimiter(args[0]);
    String argsStr = StringUtils.join(args, " ", 1, args.length);
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

  public static void broadcast(String message, String permission) {
    Set<Permissible> permissibles = Bukkit.getPluginManager().getPermissionSubscriptions("bukkit.broadcast.user");
    for (Permissible permissible : permissibles) {
      if (!(permissible instanceof BlockCommandSender) && (permissible instanceof CommandSender) && permissible.hasPermission(permission)) {
        CommandSender user = (CommandSender) permissible;
        user.sendMessage(message);
      }
    }
  }

  public static void broadcast(String message, String permission, CommandSender sender) {
    broadcast(message, permission);

    if (!sender.hasPermission(permission)) sender.sendMessage(message);
  }

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

  public static PlayerData getPlayer(CommandSender sender, String playerName) {
    boolean isUUID = isUUID(playerName);
    PlayerData player = null;

    if (isUUID) {
      try {
        player = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
      }
    } else {
      player = plugin.getPlayerStorage().retrieve(playerName, true);
    }

    return player;
  }

  public static PlayerData getActor(CommandSender sender) {
    PlayerData actor = null;

    if (sender instanceof Player) {
      try {
        actor = plugin.getPlayerStorage().queryForId(UUIDUtils.toBytes((Player) sender));
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
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

  public static void sendReportList(ReportList reports, CommandSender sender, int page) {
    String dateTimeFormat = Message.getString("report.list.row.dateTimeFormat");
    FastDateFormat dateFormatter = FastDateFormat.getInstance(dateTimeFormat);

    Message.get("report.list.row.header")
           .set("page", page)
           .set("maxPage", reports.getMaxPage())
           .set("count", reports.getCount())
           .sendTo(sender);

    for (PlayerReportData report : reports.getList()) {
      Message.get("report.list.row.all")
             .set("id", report.getId())
             .set("state", report.getState().getName())
             .set("player", report.getPlayer().getName())
             .set("actor", report.getActor().getName())
             .set("reason", report.getReason())
             .set("created", dateFormatter
                     .format(report.getCreated() * 1000L))
             .set("updated", dateFormatter
                     .format(report.getUpdated() * 1000L))
             .sendTo(sender);
    }
  }
}
