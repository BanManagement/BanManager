package me.confuser.banmanager.common.util;

import java.time.Duration;

/**
 * Utility class for scheduler time conversions.
 * Provides consistent Duration to ticks conversion across tick-based platforms (Bukkit, Sponge).
 */
public final class SchedulerTime {

  private SchedulerTime() {
    // Utility class - prevent instantiation
  }

  /**
   * Converts a Duration to Minecraft ticks using ceiling rounding.
   * This ensures delays are never shortened due to truncation.
   *
   * @param duration the duration to convert
   * @return the number of ticks (20 ticks = 1 second, 1 tick = 50ms)
   */
  public static long durationToTicksCeil(Duration duration) {
    long millis = duration.toMillis();
    if (millis <= 0) {
      return 0;
    }
    // Ceiling division: (millis + 49) / 50 ensures we round up
    return (millis + 49) / 50;
  }
}
