package me.confuser.banmanager.common.util;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class MessageRegistryTest {

  private MessageRegistry registry;

  @Before
  public void setUp() {
    registry = new MessageRegistry("en");

    Map<String, String> en = new HashMap<>();
    en.put("greeting", "Hello");
    en.put("farewell", "Goodbye");
    en.put("only.in.english", "English only");
    registry.loadLocale("en", en);

    Map<String, String> zh = new HashMap<>();
    zh.put("greeting", "你好");
    zh.put("farewell", "再见");
    registry.loadLocale("zh", zh);

    Map<String, String> zhTw = new HashMap<>();
    zhTw.put("greeting", "你好 (繁體)");
    registry.loadLocale("zh_tw", zhTw);
  }

  @Test
  public void cascadingFallbackExactLocale() {
    assertEquals("你好 (繁體)", registry.getMessage("greeting", "zh_tw"));
  }

  @Test
  public void cascadingFallbackToBaseLanguage() {
    assertEquals("再见", registry.getMessage("farewell", "zh_tw"));
  }

  @Test
  public void cascadingFallbackToDefault() {
    assertEquals("English only", registry.getMessage("only.in.english", "zh_tw"));
  }

  @Test
  public void missingKeyFallbackToDefault() {
    assertEquals("English only", registry.getMessage("only.in.english", "de"));
  }

  @Test
  public void unknownLocaleReturnsDefault() {
    assertEquals("Hello", registry.getMessage("greeting", "fr"));
  }

  @Test
  public void defaultLocaleReturnsDirectly() {
    assertEquals("Hello", registry.getMessage("greeting"));
  }

  @Test
  public void missingKeyReturnsNull() {
    assertNull(registry.getMessage("nonexistent", "en"));
  }

  @Test
  public void availableLocales() {
    Set<String> locales = registry.getAvailableLocales();
    assertTrue(locales.contains("en"));
    assertTrue(locales.contains("zh"));
    assertTrue(locales.contains("zh_tw"));
    assertEquals(3, locales.size());
  }

  @Test
  public void missingKeyCount() {
    assertEquals(1, registry.getMissingKeyCount("zh"));
    assertEquals(2, registry.getMissingKeyCount("zh_tw"));
    assertEquals(0, registry.getMissingKeyCount("en"));
  }

  @Test
  public void atomicSwapReplacesContent() {
    MessageRegistry newRegistry = new MessageRegistry("de");
    Map<String, String> de = new HashMap<>();
    de.put("greeting", "Hallo");
    newRegistry.loadLocale("de", de);

    registry.atomicSwap(newRegistry);

    assertEquals("de", registry.getDefaultLocale());
    assertEquals("Hallo", registry.getMessage("greeting"));
    assertNull(registry.getMessage("farewell"));
  }

  @Test
  public void putMessageUpdatesExistingLocale() {
    registry.putMessage("greeting", "Hi", "en");
    assertEquals("Hi", registry.getMessage("greeting", "en"));
  }

  @Test
  public void putMessageCreatesNewLocale() {
    registry.putMessage("greeting", "Bonjour", "fr");
    assertEquals("Bonjour", registry.getMessage("greeting", "fr"));
    assertTrue(registry.getAvailableLocales().contains("fr"));
  }

  @Test
  public void hasAnyMessagesReturnsTrueWhenLoaded() {
    assertTrue(registry.hasAnyMessages());
  }

  @Test
  public void hasAnyMessagesReturnsFalseWhenEmpty() {
    MessageRegistry empty = new MessageRegistry("en");
    assertFalse(empty.hasAnyMessages());
  }

  @Test
  public void defaultLocaleMismatchStillResolvesViaFallback() {
    MessageRegistry reg = new MessageRegistry("en_us");
    Map<String, String> en = new HashMap<>();
    en.put("test", "Hello");
    reg.loadLocale("en", en);

    assertEquals("Hello", reg.getMessage("test", "en_us"));
    assertTrue(reg.hasAnyMessages());
  }
}
