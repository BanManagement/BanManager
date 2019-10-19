package me.confuser.banmanager.common;

import ch.vorburger.mariadb4j.junit.MariaDB4jRule;
import com.github.javafaker.Faker;
import me.confuser.banmanager.common.configs.PluginInfo;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class BasePluginDbTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @ClassRule
  public static MariaDB4jRule dbRule = new MariaDB4jRule(0); //port 0 means select random available port
  protected BanManagerPlugin plugin;
  protected Faker faker = new Faker();
  protected TestUtils testUtils;
  private boolean configSetup = false;

  @Before
  public void setup() throws Exception {
    TestServer server = new TestServer();
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(new PluginInfo(), logger, temporaryFolder.getRoot(), new TestScheduler(), server);

    testUtils = new TestUtils(plugin, faker);
    server.enable(plugin);

    if (!configSetup) {
      try {
        plugin.enable();
      } catch (Exception e) {
      }

      setupConfig();
    }

    plugin.enable();
  }

  private void setupConfig() throws Exception {
    Path configFile = new File(temporaryFolder.getRoot(), "config.yml").toPath();
    List<String> lines = Files.readAllLines(configFile, StandardCharsets.UTF_8);
    lines.set(5, "    enabled: true");
    lines.set(6, "    storageType: mariadb");
    lines.set(7, "    host: localhost");
    lines.set(8, "    port: " + dbRule.getDBConfiguration().getPort());
    lines.set(9, "    name: test");
    lines.set(10, "    user: root");
    lines.set(11, "    password: ''");

    Files.write(configFile, lines, StandardCharsets.UTF_8);

    configSetup = true;
  }
}
