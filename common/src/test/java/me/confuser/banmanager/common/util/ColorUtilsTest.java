package me.confuser.banmanager.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ColorUtilsTest {

  @Test
  public void testHexColorParsing() {
    String input = "&#ff0000Red Text";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertFalse(legacy.contains("#"), "Output should not contain #");
    assertTrue(legacy.contains("Red Text"), "Output should contain 'Red Text'");
  }

  @Test
  public void testMixedColors() {
    String input = "&cRed &#00ff00Green &9Blue";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertFalse(legacy.contains("#"), "Output should not contain #");
    assertTrue(legacy.contains("Red"), "Output should contain text");
    assertTrue(legacy.contains("Green"), "Output should contain text");
    assertTrue(legacy.contains("Blue"), "Output should contain text");
  }

  @Test
  public void testSpigotHexFormat() {
    String input = "&x&f&f&0&0&0&0Red";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertFalse(legacy.contains("&x"), "Output should not contain &x");
    assertFalse(legacy.contains("#"), "Output should not contain #");
    assertTrue(legacy.contains("Red"), "Output should contain 'Red'");
  }

  @Test
  public void testDownsampledJsonNoHex() {
    String input = "&#ff5733Orange";
    String json = ColorUtils.toDownsampledJson(input);
    assertFalse(json.contains("#ff5733"), "JSON should not contain hex code");
    assertFalse(json.contains("ff5733"), "JSON should not contain hex code");
    assertTrue(json.contains("Orange"), "JSON should contain text");
  }

  @Test
  public void testFullJsonHasHex() {
    String input = "&#ff5733Orange";
    String json = ColorUtils.toJson(input);
    assertTrue(json.contains("Orange"), "JSON should contain text");
  }

  @Test
  public void testNewlinePreservation() {
    String input = "Line1\\nLine2";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertTrue(legacy.contains("\n"), "Should convert \\n to newline");
  }

  @Test
  public void testPlainTextUnchanged() {
    String input = "Hello World";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertEquals("Hello World", legacy, "Plain text should be unchanged");
  }

  @Test
  public void testLegacyColorsPreserved() {
    String input = "&cRed &aGreen";
    String legacy = ColorUtils.toDownsampledLegacy(input);
    assertTrue(legacy.contains("&"), "Should contain & codes");
    assertTrue(legacy.contains("Red"), "Should contain text");
    assertTrue(legacy.contains("Green"), "Should contain text");
  }
}
