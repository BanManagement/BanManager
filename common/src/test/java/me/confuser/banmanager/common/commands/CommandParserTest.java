package me.confuser.banmanager.common.commands;

import me.confuser.banmanager.common.BasePluginTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class CommandParserTest extends BasePluginTest {

  @Test
  public void shouldParseOnlineOnlyFlag() {
    String[] args = new String[] { "confuser", "1d", "-o", "test reason" };
    CommandParser parser = new CommandParser(this.plugin, args, 2);

    assertTrue(parser.isOnlineOnly());
    assertEquals("confuser", parser.getArgs()[0]);
    assertEquals("1d", parser.getArgs()[1]);
    assertEquals("test reason", parser.getReason().getMessage());
  }

  @Test
  public void shouldParseOnlineOnlyFlagWithAlias() {
    String[] args = new String[] { "confuser", "1d", "-o", "reason here" };
    CommandParser parser = new CommandParser(this.plugin, args, 2);

    assertTrue(parser.isOnlineOnly());
  }

  @Test
  public void shouldDefaultOnlineOnlyToFalse() {
    String[] args = new String[] { "confuser", "1d", "test reason" };
    CommandParser parser = new CommandParser(this.plugin, args, 2);

    assertFalse(parser.isOnlineOnly());
    assertEquals("test reason", parser.getReason().getMessage());
  }

  @Test
  public void shouldParseSilentFlag() {
    String[] args = new String[] { "confuser", "1d", "-s", "silent reason" };
    CommandParser parser = new CommandParser(this.plugin, args, 2);

    assertTrue(parser.isSilent());
    assertFalse(parser.isOnlineOnly());
  }

  @Test
  public void shouldParseSoftFlag() {
    String[] args = new String[] { "confuser", "1d", "-st", "soft reason" };
    CommandParser parser = new CommandParser(this.plugin, args, 2);

    assertTrue(parser.isSoft());
    assertFalse(parser.isOnlineOnly());
  }

  @Test
  public void shouldParseMultipleFlags() {
    String[] args = new String[] { "confuser", "1d", "-s", "-o", "silent online reason" };
    CommandParser parser = new CommandParser(this.plugin, args, 2);

    assertTrue(parser.isSilent());
    assertTrue(parser.isOnlineOnly());
    assertEquals("silent online reason", parser.getReason().getMessage());
  }

  @Test
  public void shouldParseFlagsInAnyOrder() {
    String[] args = new String[] { "confuser", "1d", "-o", "-s", "-st", "all flags reason" };
    CommandParser parser = new CommandParser(this.plugin, args, 2);

    assertTrue(parser.isSilent());
    assertTrue(parser.isSoft());
    assertTrue(parser.isOnlineOnly());
    assertEquals("all flags reason", parser.getReason().getMessage());
  }
}
