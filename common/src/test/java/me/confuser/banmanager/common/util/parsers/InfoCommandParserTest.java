package me.confuser.banmanager.common.util.parsers;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InfoCommandParserTest extends BasePluginTest {

  @Test
  public void parsesKicksFlag() {
    String[] args = new String[] { "confuser", "-k" };
    InfoCommandParser parser = new InfoCommandParser(this.plugin, args, 1);

    assert (parser.isKicks());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesWarningsFlag() {
    String[] args = new String[] { "confuser", "-w" };
    InfoCommandParser parser = new InfoCommandParser(this.plugin, args, 1);

    assert (parser.isWarnings());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesBansFlag() {
    String[] args = new String[] { "confuser", "-b" };
    InfoCommandParser parser = new InfoCommandParser(this.plugin, args, 1);

    assert (parser.isBans());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesMutesFlag() {
    String[] args = new String[] { "confuser", "-m" };
    InfoCommandParser parser = new InfoCommandParser(this.plugin, args, 1);

    assert (parser.isMutes());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesNotesFlag() {
    String[] args = new String[] { "confuser", "-n" };
    InfoCommandParser parser = new InfoCommandParser(this.plugin, args, 1);

    assert (parser.isNotes());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesTimeFlag() {
    String[] args = new String[] { "confuser", "-t", "5d" };
    InfoCommandParser parser = new InfoCommandParser(this.plugin, args, 1);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals("5d", parser.getTime());
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesIpsFlag() {
    String[] args = new String[] { "confuser", "-i", "5" };
    InfoCommandParser parser = new InfoCommandParser(this.plugin, args, 1);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(5, parser.getIps(), 0);
    assertEquals(1, parser.getArgs().length);
  }
}
