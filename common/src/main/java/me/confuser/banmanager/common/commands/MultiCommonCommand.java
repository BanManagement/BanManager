package me.confuser.banmanager.common.commands;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public abstract class MultiCommonCommand extends CommonCommand {

  @Getter
  private HashMap<String, CommonSubCommand> commands = new HashMap<>();

  // Custom messages, allow whatever is using this to override
  private String commandMessage = "BanManager";
  private String commandTypeHelpMessage;
  private String commandNoExistMessage = "Command doesn't exist.";
  private String errorOccuredMessage = "An error occured while executing the command. Check the console";
  private String noPermissionMessage = "You do not have permission for this command";

  public MultiCommonCommand(BanManagerPlugin plugin, String commandName) {
    super(plugin, commandName, false);

    commandTypeHelpMessage = "Type /" + commandName + " help for help";
  }

  public abstract void registerCommands();

  public void commandNotFound(CommonSender sender, CommandParser args) {
    sender.sendMessage(commandNoExistMessage);
    sender.sendMessage(commandTypeHelpMessage);
  }

  public void registerCommonSubCommand(CommonSubCommand command) {
    commands.put(command.getName(), command);
  }

  @Override
  public boolean onCommand(CommonSender sender, CommandParser args) {
    if (args == null || args.getArgs().length < 1) {
      sender.sendMessage(commandMessage);
      sender.sendMessage(commandTypeHelpMessage);
      return true;
    }

    if (args.getArgs()[0].equalsIgnoreCase("help")) {
      help(sender);
      return true;
    }

    String sub = args.getArgs()[0].toLowerCase();

    // Remove sub from args
    Vector<String> vec = new Vector<>();
    vec.addAll(Arrays.asList(args.getArgs()));
    vec.remove(0);
    String[] subArgs = vec.toArray(new String[0]);

    // Clean up
    vec = null;

    if (!commands.containsKey(sub)) {
      commandNotFound(sender, args);
      return true;
    }
    try {
      CommonSubCommand command = commands.get(sub);

      if (!hasPermission(sender, command))
        sender.sendMessage(noPermissionMessage);
      else {
        boolean showHelp = command.onCommand(sender, new CommandParser(getPlugin(), subArgs));

        if (!showHelp && command.getHelp() != null) {
          sender.sendMessage("/" + getCommandName() + " " + command.getHelp());
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      sender.sendMessage(errorOccuredMessage);
      sender.sendMessage(commandTypeHelpMessage);
    }
    return true;
  }

  private boolean hasPermission(CommonSender sender, CommonSubCommand command) {
    return sender.hasPermission("bm." + command.getPermission());
  }

  public void help(CommonSender p) {
    p.sendMessage("/" + getCommandName() + " <command> <args>");
    p.sendMessage("Commands are as follows:");

    for (CommonSubCommand v : commands.values()) {
      if (hasPermission(p, v) && v.getHelp() != null)
        p.sendMessage(v.getName() + " " + v.getHelp());
    }
  }
}
