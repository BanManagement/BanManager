package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonMetrics;
import org.bstats.sponge.Metrics2;

import java.util.HashMap;
import java.util.Map;

public class SpongeMetrics implements CommonMetrics {
  private final Metrics2 metrics;

  public SpongeMetrics(Metrics2 metrics) {
    this.metrics = metrics;
  }

  @Override
  public void submitOnlineMode(boolean online) {
    metrics.addCustomChart((new Metrics2.SimplePie("banmanagerMode", () -> online ? "online" : "offline")));
  }

  @Override
  public void submitStorageType(String storageType) {
    metrics.addCustomChart((new Metrics2.SimplePie("storageType", () -> storageType)));
  }

  @Override
  public void submitStorageVersion(String version) {
    metrics.addCustomChart((new Metrics2.DrilldownPie("storageVersion", () -> {
      Map<String, Map<String, Integer>> map = new HashMap<>();

      Map<String, Integer> entry = new HashMap<>();
      entry.put(version, 1);

      if (version.contains("Maria")) {
        map.put("MariaDB", entry);
      } else {
        map.put("MySQL", entry);
      }

      return map;
    })));
  }

  @Override
  public void submitGlobalMode(boolean enabled) {
    metrics.addCustomChart((new Metrics2.SimplePie("globalMode", () -> enabled ? "enabled" : "disabled")));
  }

  @Override
  public void submitGeoMode(boolean enabled) {
    metrics.addCustomChart((new Metrics2.SimplePie("geoMode", () -> enabled ? "enabled" : "disabled")));
  }

  @Override
  public void submitDiscordMode(boolean enabled) {
    metrics.addCustomChart((new Metrics2.SimplePie("discordMode", () -> enabled ? "enabled" : "disabled")));
  }
}
