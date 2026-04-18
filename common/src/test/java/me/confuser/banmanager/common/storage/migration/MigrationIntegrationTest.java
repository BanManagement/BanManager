package me.confuser.banmanager.common.storage.migration;

import me.confuser.banmanager.common.*;
import me.confuser.banmanager.common.configs.PluginInfo;
import me.confuser.banmanager.common.ormlite.dao.Dao;
import me.confuser.banmanager.common.ormlite.dao.DaoManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.spy;

public class MigrationIntegrationTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private BanManagerPlugin plugin;
  private TestServer server = spy(new TestServer());

  @Before
  public void setup() throws Exception {
    CommonLogger logger = new TestLogger();
    PluginInfo pluginInfo = BasePluginTest.setupConfigs(temporaryFolder);
    plugin = new BanManagerPlugin(pluginInfo, logger, temporaryFolder.getRoot(), new TestScheduler(), server, new TestMetrics());
    server.enable(plugin);
  }

  @After
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

    assertFalse("Schema versions should have been recorded", versions.isEmpty());

    boolean hasLocal = false;
    for (SchemaVersion sv : versions) {
      if ("local".equals(sv.getScope())) {
        hasLocal = true;
        assertEquals("Fresh install should mark at latest version", 2, sv.getVersion());
        assertTrue("Description should indicate fresh install", sv.getDescription().contains("fresh install"));
      }
    }

    assertTrue("Should have a local scope version", hasLocal);
  }

  @Test
  public void secondEnable_isIdempotent() throws Exception {
    plugin.enable();
    plugin.disable();

    CommonLogger logger = new TestLogger();
    PluginInfo pluginInfo = BasePluginTest.setupConfigs(temporaryFolder);
    plugin = new BanManagerPlugin(pluginInfo, logger, temporaryFolder.getRoot(), new TestScheduler(), server, new TestMetrics());
    server.enable(plugin);
    plugin.enable();

    Dao<SchemaVersion, Integer> dao = DaoManager.createDao(plugin.getLocalConn(), SchemaVersion.class);

    String[] rawResults = dao.queryRaw(
        "SELECT COUNT(*) FROM " + MigrationRunner.SCHEMA_TABLE + " WHERE scope = ?", "local"
    ).getFirstResult();

    int count = Integer.parseInt(rawResults[0]);
    assertEquals("Should have exactly one local version row (no duplicates from second enable)", 1, count);
  }
}
