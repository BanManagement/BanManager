package me.confuser.banmanager.common.storage.migration;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.ormlite.dao.Dao;
import me.confuser.banmanager.common.ormlite.dao.DaoManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

public class MigrationIntegrationTest {

  @TempDir
  public File temporaryFolder;
  private BanManagerPlugin plugin;
  private TestServer server = spy(new TestServer());

  @BeforeEach
  public void setup() throws Exception {
    CommonLogger logger = new TestLogger();
    PluginInfo pluginInfo = BasePluginTest.setupConfigs(temporaryFolder);
    plugin = new BanManagerPlugin(pluginInfo, logger, temporaryFolder, new TestScheduler(), server, new TestMetrics());
    server.enable(plugin);
  }

  @AfterEach
  public void cleanup() {
    if (plugin != null) {
      plugin.disable();
    }
  }

  @Test
  public void freshInstall_marksLatestVersion() throws Exception {
    plugin.enable();

    Dao<SchemaVersion, Integer> dao = DaoManager.createDao(plugin.getLocalConn(), SchemaVersion.class);
    List<SchemaVersion> versions = dao.queryForAll();

    assertFalse(versions.isEmpty(), "Schema versions should have been recorded");

    boolean hasLocal = false;
    for (SchemaVersion sv : versions) {
      if ("local".equals(sv.getScope())) {
        hasLocal = true;
        assertEquals(2, sv.getVersion(), "Fresh install should mark at latest version");
        assertTrue(sv.getDescription().contains("fresh install"), "Description should indicate fresh install");
      }
    }

    assertTrue(hasLocal, "Should have a local scope version");
  }

  @Test
  public void secondEnable_isIdempotent() throws Exception {
    plugin.enable();
    plugin.disable();

    CommonLogger logger = new TestLogger();
    PluginInfo pluginInfo = BasePluginTest.setupConfigs(temporaryFolder);
    plugin = new BanManagerPlugin(pluginInfo, logger, temporaryFolder, new TestScheduler(), server, new TestMetrics());
    server.enable(plugin);
    plugin.enable();

    Dao<SchemaVersion, Integer> dao = DaoManager.createDao(plugin.getLocalConn(), SchemaVersion.class);

    String[] rawResults = dao.queryRaw(
        "SELECT COUNT(*) FROM " + MigrationRunner.SCHEMA_TABLE + " WHERE scope = ?", "local"
    ).getFirstResult();

    int count = Integer.parseInt(rawResults[0]);
    assertEquals(1, count, "Should have exactly one local version row (no duplicates from second enable)");
  }
}
