package me.confuser.banmanager.common.util;

import org.junit.Test;

import java.time.Duration;

import static org.junit.Assert.assertEquals;

/**
 * Tests for SchedulerTime utility class.
 * Verifies Duration to ticks conversion using ceiling rounding policy.
 */
public class SchedulerTimeTest {

  @Test
  public void negativeDurationReturnsZero() {
    Duration negative = Duration.ofMillis(-1);
    assertEquals("Negative duration should return 0 ticks", 0, SchedulerTime.durationToTicksCeil(negative));
  }

  @Test
  public void zeroDurationReturnsZero() {
    Duration zero = Duration.ZERO;
    assertEquals("Zero duration should return 0 ticks", 0, SchedulerTime.durationToTicksCeil(zero));
  }

  @Test
  public void oneMillisecondReturnsOneTick() {
    // 1ms should ceil to 1 tick (since 1ms > 0, it needs at least 1 tick)
    Duration oneMs = Duration.ofMillis(1);
    assertEquals("1ms should ceil to 1 tick", 1, SchedulerTime.durationToTicksCeil(oneMs));
  }

  @Test
  public void fortyNineMillisecondsReturnsOneTick() {
    // 49ms should ceil to 1 tick
    Duration ms49 = Duration.ofMillis(49);
    assertEquals("49ms should ceil to 1 tick", 1, SchedulerTime.durationToTicksCeil(ms49));
  }

  @Test
  public void fiftyMillisecondsReturnsOneTick() {
    // 50ms = exactly 1 tick
    Duration ms50 = Duration.ofMillis(50);
    assertEquals("50ms should equal 1 tick", 1, SchedulerTime.durationToTicksCeil(ms50));
  }

  @Test
  public void fiftyOneMillisecondsReturnsTwoTicks() {
    // 51ms should ceil to 2 ticks
    Duration ms51 = Duration.ofMillis(51);
    assertEquals("51ms should ceil to 2 ticks", 2, SchedulerTime.durationToTicksCeil(ms51));
  }

  @Test
  public void nineHundredNinetyNineMillisecondsReturnsTwentyTicks() {
    // 999ms should ceil to 20 ticks (999 + 49 = 1048, 1048 / 50 = 20.96 -> 20)
    Duration ms999 = Duration.ofMillis(999);
    assertEquals("999ms should ceil to 20 ticks", 20, SchedulerTime.durationToTicksCeil(ms999));
  }

  @Test
  public void oneSecondReturnsTwentyTicks() {
    // 1000ms = exactly 20 ticks
    Duration oneSecond = Duration.ofSeconds(1);
    assertEquals("1 second should equal 20 ticks", 20, SchedulerTime.durationToTicksCeil(oneSecond));
  }

  @Test
  public void oneSecondAndOneMillisReturnsTwentyOneTicks() {
    // 1001ms should ceil to 21 ticks
    Duration ms1001 = Duration.ofMillis(1001);
    assertEquals("1001ms should ceil to 21 ticks", 21, SchedulerTime.durationToTicksCeil(ms1001));
  }

  @Test
  public void fiveSecondsReturnsOneHundredTicks() {
    // 5000ms = 100 ticks
    Duration fiveSeconds = Duration.ofSeconds(5);
    assertEquals("5 seconds should equal 100 ticks", 100, SchedulerTime.durationToTicksCeil(fiveSeconds));
  }
}
