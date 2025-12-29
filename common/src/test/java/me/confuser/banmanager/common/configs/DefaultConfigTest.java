package me.confuser.banmanager.common.configs;

import me.confuser.banmanager.common.BasePluginDbTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class DefaultConfigTest extends BasePluginDbTest {

  @Test
  public void shouldLoadDefaultConfig() {
    // Config should have loaded without exceptions
    assertNotNull(plugin.getConfig());
  }

  @Test
  public void shouldValidateTimeLimits() {
    // Time limits should be loaded
    TimeLimitsConfig timeLimits = plugin.getConfig().getTimeLimits();
    assertNotNull(timeLimits);
  }

  @Test
  public void shouldParseWarningActions() {
    // Warning actions config should be accessible
    DefaultConfig config = plugin.getConfig();
    assertNotNull(config);
    assertNotNull(config.getWarningActions());
  }

  @Test
  public void shouldLoadExemptions() {
    // Exemptions config should be loaded
    ExemptionsConfig exemptions = plugin.getExemptionsConfig();
    assertNotNull(exemptions);
  }

  @Test
  public void shouldValidateDatabaseConfig() {
    // Database should be accessible (if we got this far, DB is configured)
    assertNotNull(plugin.getLocalConn());
  }
}
