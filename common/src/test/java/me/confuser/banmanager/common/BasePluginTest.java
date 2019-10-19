package me.confuser.banmanager.common;

import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.*;

public abstract class BasePluginTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  protected BanManagerPlugin plugin;

  @Before
  public void setup() {
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(setupConfigs(temporaryFolder), logger, temporaryFolder.getRoot(), new TestScheduler(), new TestServer());

    try {
      plugin.enable();
    } catch (Exception e) {
    }
  }

  public static PluginInfo setupConfigs(TemporaryFolder folder) {
    String[] configs = new String[]{
        "config.yml",
        "console.yml",
        "exemptions.yml",
        "geoip.yml",
        "messages.yml",
        "reasons.yml",
        "schedules.yml"
    };

    for (String name : configs) {
      try {
        InputStream in = BasePluginTest.class.getClassLoader().getResource(name).openStream();
        File outFile = new File(folder.getRoot(), name);
        OutputStream out = new FileOutputStream(outFile);
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
        out.close();
        in.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Load plugin.yml
    PluginInfo pluginInfo = new PluginInfo();
    Reader defConfigStream = null;
    try {
      defConfigStream = new InputStreamReader(BasePluginTest.class.getClassLoader().getResource("plugin.yml").openStream());
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
    ConfigurationSection commands = conf.getConfigurationSection("commands");

    for (String command : commands.getKeys(false)) {
      ConfigurationSection cmd = commands.getConfigurationSection(command);

      pluginInfo.setCommand( new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
    }

    return pluginInfo;
  }
}
