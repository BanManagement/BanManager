package me.confuser.banmanager.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PluginTest extends BasePluginTest {
  @Test
  public void testConfigs() {
    assertFalse(plugin.getConfig().getLocalDb().isEnabled());
    assertEquals("Console", plugin.getConsoleConfig().getName());
  }
}
