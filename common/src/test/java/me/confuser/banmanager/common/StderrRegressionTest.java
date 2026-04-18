package me.confuser.banmanager.common;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies that plugin startup does not write to System.err via
 * leftover e.printStackTrace() calls.
 */
public class StderrRegressionTest extends BasePluginDbTest {

  private PrintStream originalErr;
  private ByteArrayOutputStream errCapture;

  @BeforeEach
  @Override
  public void setup() throws Exception {
    originalErr = System.err;
    errCapture = new ByteArrayOutputStream();
    System.setErr(new PrintStream(errCapture));

    super.setup();
  }

  @AfterEach
  @Override
  public void cleanup() {
    super.cleanup();
    System.setErr(originalErr);
  }

  @Test
  public void pluginStartupShouldNotWriteToStderr() {
    String stderrOutput = errCapture.toString();

    if (!stderrOutput.isEmpty()) {
      // JUL's ConsoleHandler writes to System.err by default, so we must
      // filter out JUL log lines and only flag actual e.printStackTrace() output.
      // A JUL line looks like: "Apr 03, 2026 7:41:41 PM me.confuser... info"
      // A stack trace line starts with whitespace + "at " or is a Throwable classname.
      String[] lines = stderrOutput.split("\n");
      StringBuilder offendingLines = new StringBuilder();
      for (String line : lines) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) continue;
        boolean isStackTrace = trimmed.startsWith("at ") || trimmed.startsWith("Caused by:");
        boolean isPrintStackTrace = line.contains("printStackTrace");
        boolean isExceptionHeader = trimmed.matches("^[a-z].*\\.(Exception|Error|Throwable).*");
        if (isStackTrace || isPrintStackTrace || isExceptionHeader) {
          offendingLines.append(line).append("\n");
        }
      }
      assertEquals(0, offendingLines.length(), "No e.printStackTrace() output should appear during startup:\n" + offendingLines);
    }
  }
}
