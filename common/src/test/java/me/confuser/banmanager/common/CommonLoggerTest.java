package me.confuser.banmanager.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class CommonLoggerTest {

  @Test
  public void defaultSevereShouldRouteMessageAndStackTrace() {
    List<String> messages = new ArrayList<>();
    CommonLogger logger = new CommonLogger() {
      @Override public void info(String s) {}
      @Override public void warning(String s) {}
      @Override public void severe(String s) { messages.add(s); }
    };

    Exception ex = new RuntimeException("test error");
    logger.severe("something failed", ex);

    assertEquals(2, messages.size());
    assertEquals("something failed", messages.get(0));
    assertTrue(messages.get(1).contains("java.lang.RuntimeException: test error"));
    assertTrue(messages.get(1).contains("CommonLoggerTest"));
  }

  @Test
  public void defaultWarningShouldRouteMessageAndStackTrace() {
    List<String> messages = new ArrayList<>();
    CommonLogger logger = new CommonLogger() {
      @Override public void info(String s) {}
      @Override public void warning(String s) { messages.add(s); }
      @Override public void severe(String s) {}
    };

    Exception ex = new RuntimeException("warn error");
    logger.warning("warn happened", ex);

    assertEquals(2, messages.size());
    assertEquals("warn happened", messages.get(0));
    assertTrue(messages.get(1).contains("java.lang.RuntimeException: warn error"));
  }

  @Test
  public void testLoggerOverrideUsesJul() {
    TestLogger testLogger = new TestLogger();

    testLogger.severe("jul severe", new RuntimeException("test"));
    testLogger.warning("jul warning", new RuntimeException("test"));

    // No assertion needed beyond no-throw; JUL handles the throwable natively
  }

  @Test
  public void defaultMethodsShouldNotWriteToStderr() {
    java.io.PrintStream originalErr = System.err;
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    System.setErr(new java.io.PrintStream(baos));

    try {
      CommonLogger logger = new CommonLogger() {
        @Override public void info(String s) {}
        @Override public void warning(String s) {}
        @Override public void severe(String s) {}
      };

      logger.severe("test", new RuntimeException("err"));
      logger.warning("test", new RuntimeException("err"));

      assertEquals("No output should go to stderr", 0, baos.size());
    } finally {
      System.setErr(originalErr);
    }
  }
}
