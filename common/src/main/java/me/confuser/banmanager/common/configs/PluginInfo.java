package me.confuser.banmanager.common.configs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginInfo {
  private Map<String, CommandInfo> commands;

  public PluginInfo() {
    commands = new HashMap<>();
  }

  public CommandInfo getCommand(String commandName) {
    return commands.get(commandName);
  }

  public CommandInfo setCommand(CommandInfo command) {
    return commands.put(command.name(), command);
  }

  public record CommandInfo(String name, String permission, String usage, List<String> aliases) {
  }
}
