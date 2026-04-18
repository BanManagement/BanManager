package me.confuser.banmanager.common.commands;


import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerNoteData;
import me.confuser.banmanager.common.ipaddr.IPAddress;
import me.confuser.banmanager.common.ipaddr.IPAddressString;
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

    this.usage = info.usage();
    this.permission = info.permission();
    this.aliases = info.aliases();
    this.parser = CommandParser.class;
  }

  public CommonCommand(BanManagerPlugin plugin, String commandName, boolean enableTabCompletion, PluginInfo pluginInfo) {
    this.plugin = plugin;
    this.commandName = commandName;
    this.enableTabCompletion = enableTabCompletion;

    PluginInfo.CommandInfo info = pluginInfo.getCommand(commandName);

    this.usage = info.usage();
    this.permission = info.permission();
    this.aliases = info.aliases();
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
        Message.get("sender.error.exception").sendTo(sender);
        BanManagerPlugin.getInstance().getLogger().warning("Failed to execute command", e);
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
    BanManagerPlugin.getInstance().getLogger().warning("Failed to execute command", e);
  }

  public static void handlePrivateNotes(PlayerData player, PlayerData actor, Reason reason) {
    if (BanManagerPlugin.getInstance().getConfig().isCreateNoteReasons())
      if (reason.getNotes().size() == 0) return;

    for (String note : reason.getNotes()) {
      try {
        BanManagerPlugin.getInstance().getPlayerNoteStorage().create(new PlayerNoteData(player, actor, note));
      } catch (SQLException e) {
        BanManagerPlugin.getInstance().getLogger().warning("Failed to execute command", e);
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

  public CommandParser getParser(List<String> args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    return getParser(args.toArray(new String[0]));
  }

  private static final String[] DURATION_PRESETS = {"1h", "6h", "12h", "1d", "3d", "7d", "14d", "30d", "90d", "1y"};

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
    if(args.length > 1) {
      String lookup = args[args.length - 1];
      if(lookup.startsWith("#")) {
        return plugin.getReasonsConfig().getReasons().keySet().stream().map(k -> "#" + k)
                .filter(k -> k.startsWith(lookup)).collect(Collectors.toList());
      }
    }

    if (mostLike.size() > 100) return mostLike.subList(0, 99);

    return mostLike;
  }

  public List<String> handleDurationTabComplete(CommonSender sender, String[] args, int durationArgIndex) {
    ArrayList<String> completions = new ArrayList<>(handlePlayerNameTabComplete(sender, args));

    if (args.length == durationArgIndex + 1) {
      String partial = args[durationArgIndex].toLowerCase();
      for (String preset : DURATION_PRESETS) {
        if (preset.startsWith(partial)) {
          completions.add(preset);
        }
      }
    }

    return completions;
  }

  public long getCooldown() {
    return plugin.getConfig().getCooldownsConfig().getCommand(getCommandName());
  }

  public abstract boolean onCommand(final CommonSender sender, CommandParser args);
}
