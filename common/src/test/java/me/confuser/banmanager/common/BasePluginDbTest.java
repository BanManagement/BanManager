package me.confuser.banmanager.common;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import com.github.javafaker.Faker;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class BasePluginDbTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  protected static String storageType = System.getProperty("STORAGE_TYPE", "mariadb");
  protected BanManagerPlugin plugin;
  protected Faker faker = new Faker();
  protected TestUtils testUtils;
  private boolean configSetup = false;
  private static DB db;

  @BeforeClass
  public static void dbSetup() throws ManagedProcessException {
    if (storageType.equals("mariadb")) {
      db = DB.newEmbeddedDB(0);
      db.start();
    }
  }

  @Before
  public void setup() throws Exception {
    TestServer server = new TestServer();
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(BasePluginTest.setupConfigs(temporaryFolder), logger, temporaryFolder.getRoot(), new TestScheduler(), server, new TestMetrics());

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

  @AfterClass
  public static void teardown() {
    if (db == null) return;

    try {
      db.stop();
    } catch (ManagedProcessException e) {
      throw new AssertionError("db.stop() failed", e);
    }
  }

  private void setupConfig() throws Exception {
    Path configFile = new File(temporaryFolder.getRoot(), "config.yml").toPath();
    List<String> lines = Files.readAllLines(configFile, StandardCharsets.UTF_8);
    lines.set(5, "    enabled: true");
    lines.set(6, "    storageType: " + storageType);
    lines.set(7, "    host: localhost");
    lines.set(8, "    port: " + (db != null ? db.getConfiguration().getPort() : ""));
    lines.set(9, "    name: test");
    lines.set(10, "    user: root");
    lines.set(11, "    password: ''");

    Files.write(configFile, lines, StandardCharsets.UTF_8);

    configSetup = true;
  }
}
