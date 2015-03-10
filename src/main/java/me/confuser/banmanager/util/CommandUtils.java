package me.confuser.banmanager.util;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class CommandUtils {

  public static void dispatchCommand(CommandSender sender, String command) {
    Bukkit.dispatchCommand(sender, command);
  }

  public static void dispatchCommands(CommandSender sender, List<String> commands) {
    for (String command : commands) {
      dispatchCommand(sender, command);
    }
  }

  public static void handleMultipleNames(CommandSender sender, String commandName, String[] args) {
    String delimiter;

    if (args[0].contains("|")) {
      delimiter = "\\|";
    } else {
      delimiter = "\\,";
    }

    String[] names = args[0].split(delimiter);
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
}
