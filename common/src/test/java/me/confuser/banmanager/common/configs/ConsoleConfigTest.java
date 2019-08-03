package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ConsoleConfigTest extends BasePluginTest {

  @Test
  public void isValid() {
    assertEquals("Console", plugin.getConsoleConfig().getName());
    assertTrue(plugin
            .getConsoleConfig().getUuid().toString()
            .matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[34][0-9a-fA-F]{3}-[89ab][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"));
  }
}
