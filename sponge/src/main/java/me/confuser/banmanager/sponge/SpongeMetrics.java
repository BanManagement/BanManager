package me.confuser.banmanager.sponge;

import me.confuser.banmanager.common.CommonMetrics;
import org.bstats.sponge.Metrics;

import java.util.HashMap;
import java.util.Map;

public class SpongeMetrics implements CommonMetrics {
  private final Metrics metrics;

  public SpongeMetrics(Metrics metrics) {
    this.metrics = metrics;
  }

  @Override
  public void submitOnlineMode(boolean online) {
    metrics.addCustomChart((new Metrics.SimplePie("banmanagerMode", () -> online ? "online" : "offline")));
  }

  @Override
  public void submitStorageType(String storageType) {
    metrics.addCustomChart((new Metrics.SimplePie("storageType", () -> storageType)));
  }

  @Override
  public void submitStorageVersion(String version) {
    metrics.addCustomChart((new Metrics.DrilldownPie("storageVersion", () -> {
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
    metrics.addCustomChart((new Metrics.SimplePie("globalMode", () -> enabled ? "enabled" : "disabled")));
  }

  @Override
  public void submitGeoMode(boolean enabled) {
    metrics.addCustomChart((new Metrics.SimplePie("geoMode", () -> enabled ? "enabled" : "disabled")));
  }

  @Override
  public void submitDiscordMode(boolean enabled) {
    metrics.addCustomChart((new Metrics.SimplePie("discordMode", () -> enabled ? "enabled" : "disabled")));
  }
}
