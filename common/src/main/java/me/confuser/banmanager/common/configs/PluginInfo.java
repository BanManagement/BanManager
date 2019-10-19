package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
    return commands.put(command.getName(), command);
  }

  @AllArgsConstructor
  public static class CommandInfo {
    @Getter
    private String name;
    @Getter
    private String permission;
    @Getter
    private String usage;
    @Getter
    private List<String> aliases;
  }
}
