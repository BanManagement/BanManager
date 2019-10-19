package me.confuser.banmanager.common.util.parsers;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnbanCommandParserTest extends BasePluginTest {

  @Test
  public void parsesDeleteFlag() {
    String[] args = new String[] { "confuser", "-d" };
    UnbanCommandParser parser = new UnbanCommandParser(this.plugin, args, 0);

    assert (parser.isDelete());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesDeleteFlagWithReason() {
    String[] args = new String[] { "confuser", "-d", "Testing", "Hello" };
    UnbanCommandParser parser = new UnbanCommandParser(this.plugin, args, 1);

    assert (parser.isDelete());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals("Testing Hello", parser.getReason().getMessage());
  }

  @Test
  public void handlesNoReason() {
    String[] args = new String[] { "confuser" };
    UnbanCommandParser parser = new UnbanCommandParser(this.plugin, args, 0);

    System.out.println(parser.getArgs().length);

    assertEquals(false, parser.isDelete());
    assertEquals("confuser", parser.getArgs()[0]);
  }
}
