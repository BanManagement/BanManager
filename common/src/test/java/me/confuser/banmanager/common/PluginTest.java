package me.confuser.banmanager.common;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PluginTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private BanManagerPlugin plugin;

  @Before
  public void setup() throws Exception {
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(logger, temporaryFolder.getRoot(), new TestScheduler(), new TestServer());

    plugin.enable();
  }

  @Test
  public void testConfigs() {
    assertFalse(plugin.getConfig().getLocalDb().isEnabled());
    assertEquals("Console", plugin.getConsoleConfig().getName());
  }
}
