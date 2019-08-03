package me.confuser.banmanager.common.util.parsers;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InfoCommandParserTest {

  @Test
  public void parsesKicksFlag() {
    String[] args = new String[] { "confuser", "-k" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isKicks());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesWarningsFlag() {
    String[] args = new String[] { "confuser", "-w" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isWarnings());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesBansFlag() {
    String[] args = new String[] { "confuser", "-b" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isBans());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesMutesFlag() {
    String[] args = new String[] { "confuser", "-m" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isMutes());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesNotesFlag() {
    String[] args = new String[] { "confuser", "-n" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assert (parser.isNotes());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesTimeFlag() {
    String[] args = new String[] { "confuser", "-t", "5d" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals("5d", parser.getTime());
    assertEquals(1, parser.getArgs().length);
  }

  @Test
  public void parsesIpsFlag() {
    String[] args = new String[] { "confuser", "-i", "5" };
    InfoCommandParser parser = new InfoCommandParser(args);

    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals(5, parser.getIps(), 0);
    assertEquals(1, parser.getArgs().length);
  }
}
