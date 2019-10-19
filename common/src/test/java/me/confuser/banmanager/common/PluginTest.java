package me.confuser.banmanager.common;

import me.confuser.banmanager.common.configs.PluginInfo;
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
  public void setup() {
    CommonLogger logger = new TestLogger();
    plugin = new BanManagerPlugin(new PluginInfo(), logger, temporaryFolder.getRoot(), new TestScheduler(), new TestServer());

    try {
      plugin.enable();
    } catch (Exception e) {
    }
  }

  @Test
  public void testConfigs() {
    assertFalse(plugin.getConfig().getLocalDb().isEnabled());
    assertEquals("Console", plugin.getConsoleConfig().getName());
  }
}
