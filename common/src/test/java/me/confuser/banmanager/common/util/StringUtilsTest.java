package me.confuser.banmanager.common.util;

import org.junit.Test;

import static org.junit.Assert.*;

// From apache commons lang3
public class StringUtilsTest {
  private static final String[] MIXED_STRING_LIST = new String[]{null, "", "foo"};
  private static final Object[] MIXED_TYPE_OBJECT_LIST = new Object[]{"foo", 2L};

  @Test
  public void shouldJoinAtIndex() {
    assertEquals("/", StringUtils.join(MIXED_STRING_LIST, "/", 0, MIXED_STRING_LIST.length - 1));
    assertEquals("", StringUtils.join(MIXED_STRING_LIST, "", 0, MIXED_STRING_LIST.length - 1));
    assertEquals("foo", StringUtils.join(MIXED_TYPE_OBJECT_LIST, "/", 0, 1));
    assertEquals("foo/2", StringUtils.join(MIXED_TYPE_OBJECT_LIST, "/", 0, 2));
    assertEquals("2", StringUtils.join(MIXED_TYPE_OBJECT_LIST, "/", 1, 2));
    assertEquals("", StringUtils.join(MIXED_TYPE_OBJECT_LIST, "/", 2, 1));
    assertNull(null, StringUtils.join(null, "/", 0, 1));
  }

  @Test
  public void shouldFindSubstrings() {
    String[] results = StringUtils.substringsBetween("[one], [two], [three]", "[", "]");
    assertEquals(3, results.length);
    assertEquals("one", results[0]);
    assertEquals("two", results[1]);
    assertEquals("three", results[2]);

    results = StringUtils.substringsBetween("[one], [two], three", "[", "]");
    assertEquals(2, results.length);
    assertEquals("one", results[0]);
    assertEquals("two", results[1]);

    results = StringUtils.substringsBetween("[one], [two], three]", "[", "]");
    assertEquals(2, results.length);
    assertEquals("one", results[0]);
    assertEquals("two", results[1]);

    results = StringUtils.substringsBetween("[one], two], three]", "[", "]");
    assertEquals(1, results.length);
    assertEquals("one", results[0]);

    results = StringUtils.substringsBetween("one], two], [three]", "[", "]");
    assertEquals(1, results.length);
    assertEquals("three", results[0]);

    // 'ab hello ba' will match, but 'ab non ba' won't
    // this is because the 'a' is shared between the two and can't be matched twice
    results = StringUtils.substringsBetween("aabhellobabnonba", "ab", "ba");
    assertEquals(1, results.length);
    assertEquals("hello", results[0]);

    results = StringUtils.substringsBetween("one, two, three", "[", "]");
    assertNull(results);

    results = StringUtils.substringsBetween("[one, two, three", "[", "]");
    assertNull(results);

    results = StringUtils.substringsBetween("one, two, three]", "[", "]");
    assertNull(results);

    results = StringUtils.substringsBetween("[one], [two], [three]", "[", null);
    assertNull(results);

    results = StringUtils.substringsBetween("[one], [two], [three]", null, "]");
    assertNull(results);

    results = StringUtils.substringsBetween("[one], [two], [three]", "", "");
    assertNull(results);

    results = StringUtils.substringsBetween(null, "[", "]");
    assertNull(results);

    results = StringUtils.substringsBetween("", "[", "]");
    assertEquals(0, results.length);
  }

  @Test
  public void shouldValidatePlayerNames() {
    assertTrue(StringUtils.isValidPlayerName("confuser", ""));
    assertTrue(StringUtils.isValidPlayerName("confuser", null));
    assertTrue(StringUtils.isValidPlayerName("9081", ""));
    assertTrue(StringUtils.isValidPlayerName("AaA1_23456789_0a", ""));
    assertTrue(StringUtils.isValidPlayerName(".confuser", "."));
    assertTrue(StringUtils.isValidPlayerName("+confuser", "+"));
    assertTrue(StringUtils.isValidPlayerName("-confuser", "-"));
    assertTrue(StringUtils.isValidPlayerName("!confuser", "!"));
    assertTrue(StringUtils.isValidPlayerName("*confuser", "*"));
    assertTrue(StringUtils.isValidPlayerName("+-!*.confuser", "+!-*."));

    assertFalse(StringUtils.isValidPlayerName("AaA1_23456789_0aaaa", ""));
    assertFalse(StringUtils.isValidPlayerName("confu$£r", ""));
    assertFalse(StringUtils.isValidPlayerName("127.0.0.1", ""));
    assertFalse(StringUtils.isValidPlayerName("-confuser", ""));
    assertFalse(StringUtils.isValidPlayerName("+-!*.confuser", "."));
  }
}
