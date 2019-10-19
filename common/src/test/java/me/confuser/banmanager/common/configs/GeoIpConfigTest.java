package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class GeoIpConfigTest extends BasePluginTest {

  @Test
  public void isValid() {
    assertFalse(plugin.getGeoIpConfig().isEnabled());
  }
}
