package me.confuser.banmanager.common.util.parsers;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WarnCommandParserTest extends BasePluginTest {

  @Test
  public void defaultsPointsFlag() {
    String[] args = new String[] { "confuser", "test" };
    WarnCommandParser parser = new WarnCommandParser(this.plugin, args, 1);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1.0, parser.getPoints(), 0.0);
    assertEquals(2, parser.getArgs().length);
  }

  @Test
  public void parsesPointsFlag() {
    String[] args = new String[] { "confuser", "-p 5" };
    WarnCommandParser parser = new WarnCommandParser(this.plugin, args, 1);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(5.0, parser.getPoints(), 0.0);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesPointsFlagWithReason() {
    String[] args = new String[] { "confuser", "-p 5", "Testing", "Hello" };
    WarnCommandParser parser = new WarnCommandParser(this.plugin, args, 1);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(5.0, parser.getPoints(), 0.0);
    assertEquals("Testing Hello", parser.getReason().getMessage());
  }
}
