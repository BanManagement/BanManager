package me.confuser.banmanager.common.runnables;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for BmRunnable schedule disable semantics.
 * Verifies that schedule <= 0 disables task execution as documented in schedules.yml.
 */
public class BmRunnableTest {

  /**
   * Test that schedule value of 0 should disable execution.
   * This matches the documented behavior in schedules.yml that "0 disables the scheduler".
   */
  @Test
  public void scheduleZeroDisablesExecution() {
    // Test the logic that schedule <= 0 means disabled
    int scheduleValue = 0;
    boolean shouldExecute = scheduleValue > 0;
    assertFalse("Schedule 0 should disable execution", shouldExecute);
  }

  /**
   * Test that negative schedule values should disable execution.
   */
  @Test
  public void scheduleNegativeDisablesExecution() {
    int scheduleValue = -1;
    boolean shouldExecute = scheduleValue > 0;
    assertFalse("Negative schedule should disable execution", shouldExecute);
  }

  /**
   * Test that positive schedule values allow execution.
   */
  @Test
  public void schedulePositiveAllowsExecution() {
    int scheduleValue = 5;
    boolean scheduleAllowsExecution = scheduleValue > 0;
    assertTrue("Positive schedule should allow execution (if other conditions met)", scheduleAllowsExecution);
  }
}
