package me.confuser.banmanager.common.configs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginInfo {
  private Map<String, CommandInfo> commands;

  public PluginInfo() {
    commands = new HashMap<>();

    Reader resource = new InputStreamReader(getResource("plugin.yml"), StandardCharsets.UTF_8);
    YamlConfiguration conf = YamlConfiguration.loadConfiguration(resource);
    ConfigurationSection commands = conf.getConfigurationSection("commands");

    for (String command : commands.getKeys(false)) {
      ConfigurationSection cmd = commands.getConfigurationSection(command);

      this.commands.put(command, new CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
    }
  }

  private InputStream getResource(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("Filename cannot be null");
    }

    try {
      URL url = getClass().getClassLoader().getResource(filename);

      if (url == null) {
        return null;
      }

      URLConnection connection = url.openConnection();
      connection.setUseCaches(false);
      return connection.getInputStream();
    } catch (IOException ex) {
      return null;
    }
  }

  public CommandInfo getCommand(String commandName) {
    return commands.get(commandName);
  }

  @AllArgsConstructor
  public class CommandInfo {
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
