package me.confuser.banmanager.common.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ColorUtilsTest {

  @Test
  public void testHexColorParsing() {
    String input = "&#ff0000Red Text";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertFalse("Output should not contain #", legacy.contains("#"));
    assertTrue("Output should contain 'Red Text'", legacy.contains("Red Text"));
  }

  @Test
  public void testMixedColors() {
    String input = "&cRed &#00ff00Green &9Blue";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertFalse("Output should not contain #", legacy.contains("#"));
    assertTrue("Output should contain text", legacy.contains("Red"));
    assertTrue("Output should contain text", legacy.contains("Green"));
    assertTrue("Output should contain text", legacy.contains("Blue"));
  }

  @Test
  public void testSpigotHexFormat() {
    String input = "&x&f&f&0&0&0&0Red";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertFalse("Output should not contain &x", legacy.contains("&x"));
    assertFalse("Output should not contain #", legacy.contains("#"));
    assertTrue("Output should contain 'Red'", legacy.contains("Red"));
  }

  @Test
  public void testDownsampledJsonNoHex() {
    String input = "&#ff5733Orange";
    String json = ColorUtils.toDownsampledJson(input);
    assertFalse("JSON should not contain hex code", json.contains("#ff5733"));
    assertFalse("JSON should not contain hex code", json.contains("ff5733"));
    assertTrue("JSON should contain text", json.contains("Orange"));
  }

  @Test
  public void testFullJsonHasHex() {
    String input = "&#ff5733Orange";
    String json = ColorUtils.toJson(input);
    assertTrue("JSON should contain text", json.contains("Orange"));
  }

  @Test
  public void testNewlinePreservation() {
    String input = "Line1\\nLine2";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertTrue("Should convert \\n to newline", legacy.contains("\n"));
  }

  @Test
  public void testPlainTextUnchanged() {
    String input = "Hello World";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertEquals("Plain text should be unchanged", "Hello World", legacy);
  }

  @Test
  public void testLegacyColorsPreserved() {
    String input = "&cRed &aGreen";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertTrue("Should contain & codes", legacy.contains("&"));
    assertTrue("Should contain text", legacy.contains("Red"));
    assertTrue("Should contain text", legacy.contains("Green"));
  }
}
