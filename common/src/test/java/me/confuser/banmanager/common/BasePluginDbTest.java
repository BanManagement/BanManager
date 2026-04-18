package me.confuser.banmanager.common;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import me.confuser.banmanager.common.commands.CommonCommand;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.*;

public abstract class BasePluginDbTest {
  @TempDir
  public File temporaryFolder;
  protected static String storageType = System.getenv("STORAGE_TYPE") != null ? System.getenv("STORAGE_TYPE") : "h2";
  protected BanManagerPlugin plugin;
  protected Faker faker = new Faker();
  protected TestUtils testUtils;
  protected TestServer server = spy(new TestServer());
  private static boolean configSetup = false;
  private static DB db;

  @BeforeAll
  public static void dbSetup() throws ManagedProcessException {
    if (storageType.equals("mariadb")) {
      db = DB.newEmbeddedDB(0);
      db.start();
    }
  }

  @BeforeEach
  public void setup() throws Exception {
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(BasePluginTest.setupConfigs(temporaryFolder), logger, temporaryFolder, new TestScheduler(), server, new TestMetrics());

    testUtils = new TestUtils(plugin, faker);
    server.enable(plugin);

    if (!configSetup) {
      try {
        plugin.enable();
      } catch (Exception e) {
      }

      plugin.disable();

      setupConfig();

      plugin = new BanManagerPlugin(BasePluginTest.setupConfigs(temporaryFolder), logger, temporaryFolder, new TestScheduler(), server, new TestMetrics());
      testUtils = new TestUtils(plugin, faker);
      server.enable(plugin);
    }

    plugin.enable();
  }

  @AfterEach
  public void cleanup() {
    if (plugin != null) {
      plugin.disable();
    }
  }

  @AfterAll
  public static void teardown() {
    configSetup = false;

    if (db == null) return;

    try {
      db.stop();
    } catch (ManagedProcessException e) {
      throw new AssertionError("db.stop() failed", e);
    }
  }

  /**
   * Locates a registered command by name and returns it cast to the requested
   * type, or skips the calling test (via {@link Assumptions#assumeTrue}) if
   * the command is not registered on this platform/profile.
   *
   * <p>Use from a {@code @BeforeEach} setup method so the assumption short-
   * circuits the entire test class rather than each test method having to
   * repeat the check.</p>
   */
  @SuppressWarnings("unchecked")
  protected <T extends CommonCommand> T requireCommand(String name) {
    for (CommonCommand command : plugin.getCommands()) {
      if (command.getCommandName().equals(name)) {
        return (T) command;
      }
    }
    Assumptions.assumeTrue(false, name + " command is not registered, skipping test class");
    return null;
  }

  private void setupConfig() throws Exception {
    Path configFile = new File(temporaryFolder, "config.yml").toPath();
    List<String> lines = Files.readAllLines(configFile, StandardCharsets.UTF_8);
    lines.set(5, "    storageType: " + storageType);
    lines.set(6, "    host: localhost");
    lines.set(7, "    port: " + (db != null ? db.getConfiguration().getPort() : ""));
    lines.set(8, "    name: test");
    lines.set(9, "    user: root");
    lines.set(10, "    password: ''");

    Files.write(configFile, lines, StandardCharsets.UTF_8);

    configSetup = true;
  }
}
