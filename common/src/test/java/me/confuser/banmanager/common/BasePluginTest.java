package me.confuser.banmanager.common;

import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;

public abstract class BasePluginTest {
  @TempDir
  public File temporaryFolder;
  protected BanManagerPlugin plugin;

  @BeforeEach
  public void setup() {
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(setupConfigs(temporaryFolder), logger, temporaryFolder, new TestScheduler(), new TestServer(), new TestMetrics());

    try {
      plugin.enable();
    } catch (Exception e) {
    }
  }

  @AfterEach
  public void cleanup() {
    if (plugin != null) {
      plugin.disable();
    }
  }

  public static PluginInfo setupConfigs(File folder) {
    String[] configs = new String[]{
        "config.yml",
        "console.yml",
        "webhooks.yml",
        "exemptions.yml",
        "geoip.yml",
        "notifications.yml",
        "reasons.yml",
        "schedules.yml"
    };

    for (String name : configs) {
      try (InputStream in = BasePluginTest.class.getClassLoader().getResource(name).openStream();
           OutputStream out = new FileOutputStream(new File(folder, name))) {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    File messagesDir = new File(folder, "messages");
    messagesDir.mkdirs();
    try (InputStream in = BasePluginTest.class.getClassLoader().getResource("messages/messages_en.yml").openStream();
         OutputStream out = new FileOutputStream(new File(messagesDir, "messages_en.yml"))) {
      byte[] buf = new byte[1024];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    PluginInfo pluginInfo = new PluginInfo();
    try (InputStream in = BasePluginTest.class.getClassLoader().getResource("plugin.yml").openStream();
         Reader defConfigStream = new InputStreamReader(in)) {
      YamlConfiguration conf = YamlConfiguration.loadConfiguration(defConfigStream);
      ConfigurationSection commands = conf.getConfigurationSection("commands");

      for (String command : commands.getKeys(false)) {
        ConfigurationSection cmd = commands.getConfigurationSection(command);

        pluginInfo.setCommand(new PluginInfo.CommandInfo(command, cmd.getString("permission"), cmd.getString("usage"), cmd.getStringList("aliases")));
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    return pluginInfo;
  }
}
