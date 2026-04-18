package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class GeoIpConfigTest extends BasePluginTest {

  @Test
  public void isValid() {
    assertFalse(plugin.getGeoIpConfig().isEnabled());
  }
}
