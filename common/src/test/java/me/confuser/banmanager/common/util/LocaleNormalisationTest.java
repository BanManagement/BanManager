package me.confuser.banmanager.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LocaleNormalisationTest {

  @Test
  public void enGBBecomesEnGb() {
    assertEquals("en_gb", MessageRegistry.normaliseLocale("en_GB"));
  }

  @Test
  public void zhTWBecomesZhTw() {
    assertEquals("zh_tw", MessageRegistry.normaliseLocale("zh-TW"));
  }

  @Test
  public void uppercaseBecomesLowercase() {
    assertEquals("en", MessageRegistry.normaliseLocale("EN"));
  }

  @Test
  public void ptBRBecomesPtBr() {
    assertEquals("pt_br", MessageRegistry.normaliseLocale("pt_BR"));
  }

  @Test
  public void hyphenIsReplacedWithUnderscore() {
    assertEquals("en_gb", MessageRegistry.normaliseLocale("en-gb"));
  }

  @Test
  public void alreadyNormalisedIsUnchanged() {
    assertEquals("en", MessageRegistry.normaliseLocale("en"));
  }

  @Test
  public void nullReturnsEn() {
    assertEquals("en", MessageRegistry.normaliseLocale(null));
  }

  @Test
  public void emptyStringReturnsEn() {
    assertEquals("en", MessageRegistry.normaliseLocale(""));
  }
}
