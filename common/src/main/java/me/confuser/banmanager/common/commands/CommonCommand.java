package me.confuser.banmanager.common.commands;


import inet.ipaddr.IPAddress;
import inet.ipaddr.IPAddressString;
import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.util.IPUtils;
import me.confuser.banmanager.common.util.Message;
import me.confuser.banmanager.common.util.UUIDUtils;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class CommonCommand {

  @Getter
  private BanManagerPlugin plugin;
  @Getter
  private final String usage;
  @Getter
  private final String permission;
  @Getter
  private final String commandName;
  @Getter
  private boolean enableTabCompletion;
  @Getter
  private final List<String> aliases;
  private Class parser;
  private Integer start = null;

  public CommonCommand(BanManagerPlugin plugin, String commandName, boolean enableTabCompletion) {
    this.plugin = plugin;
    this.commandName = commandName;
    this.enableTabCompletion = enableTabCompletion;

    PluginInfo.CommandInfo info = plugin.getPluginInfo().getCommand(commandName);

    this.usage = info.getUsage();
    this.permission = info.getPermission();
    this.aliases = info.getAliases();
    this.parser = CommandParser.class;
  }

  public CommonCommand(BanManagerPlugin plugin, String commandName, boolean enableTabCompletion, int start) {
    this(plugin, commandName, enableTabCompletion);

    this.start = start;
  }

  public CommonCommand(BanManagerPlugin plugin, String commandName, boolean enableTabCompletion, Class parser, int
      start) {
    this(plugin, commandName, enableTabCompletion);

    this.parser = parser;
    this.start = start;
  }

  public static boolean isUUID(String player) {
    return player.length() > 16;
  }

  public static PlayerData getPlayer(CommonSender sender, String playerName, boolean mojangLookup) {
    boolean isUUID = isUUID(playerName);
    PlayerData player = null;

    if (isUUID) {
      try {
        player = BanManagerPlugin.getInstance().getPlayerStorage().queryForId(UUIDUtils.toBytes(UUID.fromString(playerName)));
      } catch (SQLException e) {
        sender.sendMessage(Message.get("sender.error.exception").toString());
        e.printStackTrace();
      }
    } else {
      player = BanManagerPlugin.getInstance().getPlayerStorage().retrieve(playerName, mojangLookup);
    }

    return player;
  }

  public static void handlePunishmentCreateException(SQLException e, CommonSender sender, Message duplicateMessage) {
    // For some reason ORMLite hides the error code (returns 0 instead of 1062)
    if (e.getCause().getMessage().startsWith("Duplicate entry")) {
      duplicateMessage.sendTo(sender);
      return;
    }

    Message.get("sender.error.exception").sendTo(sender);
    e.printStackTrace();
  }

  public static void handlePrivateNotes(PlayerData player, PlayerData actor, Reason reason) {
    if (BanManagerPlugin.getInstance().getConfig().isCreateNoteReasons())
      if (reason.getNotes().size() == 0) return;

    for (String note : reason.getNotes()) {
      try {
        BanManagerPlugin.getInstance().getPlayerNoteStorage().create(new PlayerNoteData(player, actor, note));
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }

  }

  public static IPAddress getIp(String ipStr) {
    final boolean isName = !IPUtils.isValid(ipStr);
    IPAddress ip = null;

    if (isName) {
      PlayerData player = BanManagerPlugin.getInstance().getPlayerStorage().retrieve(ipStr, false);
      if (player == null) return null;

      ip = player.getIp();
    } else {
      ip = new IPAddressString(ipStr).getAddress();
    }

    return ip;
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

  public CommandParser getParser(String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    if (start == null) {
      return (CommandParser) parser.getDeclaredConstructor(BanManagerPlugin.class, String[].class).newInstance(plugin,
          args);
    }

    return (CommandParser) parser.getDeclaredConstructor(BanManagerPlugin.class, String[].class, int.class)
        .newInstance(plugin,
            args, start);
  }

  public List<String> handlePlayerNameTabComplete(CommonSender sender, String[] args) {
    ArrayList<String> mostLike = new ArrayList<>();
    if(args.length == 1) {
      if (isValidNameDelimiter(args[0])) {
        String[] names = splitNameDelimiter(args[0]);

        String lookup = names[names.length - 1];

        if (plugin.getConfig().isOfflineAutoComplete()) {
          for (CharSequence charSequence : plugin.getPlayerStorage().getAutoCompleteTree().getKeysStartingWith(lookup)) {
            mostLike.add(args[0] + charSequence.toString().substring(lookup.length()));
          }
        }
      } else if (plugin.getConfig().isOfflineAutoComplete()) {
        for (CharSequence charSequence : plugin.getPlayerStorage().getAutoCompleteTree().getKeysStartingWith(args[0])) {
          mostLike.add(charSequence.toString());
        }
      } else {
        CommonPlayer senderPlayer = sender instanceof CommonPlayer ? (CommonPlayer) sender : null;
        String lower = args[0].toLowerCase();
        for (CommonPlayer player : plugin.getServer().getOnlinePlayers()) {
          if ((senderPlayer == null || senderPlayer.canSee(player)) && player.getName().toLowerCase().startsWith(lower)) {
            mostLike.add(player.getName());
          }
        }
      }
    }
    if(args.length == 2) {
      // Reasons?
      // TODO: Only allow reasons for valid commands.
      String lookup = args[1];
      if(lookup.startsWith("#")) {
        return plugin.getReasonsConfig().getReasons().keySet().stream().map(k -> "#" + k)
                .filter(k -> k.startsWith(lookup)).collect(Collectors.toList());
      }
    }

    if (mostLike.size() > 100) return mostLike.subList(0, 99);

    return mostLike;
  }

  public abstract boolean onCommand(final CommonSender sender, CommandParser args);
}
