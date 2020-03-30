package me.confuser.banmanager.common;

import lombok.Getter;

import java.util.List;

public class CommonExternalCommand {
  @Getter
  private final String pluginName;
  @Getter
  private final String name;
  @Getter
  private final List<String> aliases;

  public CommonExternalCommand(String pluginName, String name, List<String> aliases) {
    this.pluginName = pluginName;
    this.name = name;
    this.aliases = aliases;
  }
}
