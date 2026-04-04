package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.TestLogger;
import me.confuser.banmanager.common.TestPlayer;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class MessageDeferredTest {

  @Before
  public void setUp() {
    MessageRegistry registry = new MessageRegistry("en");

    Map<String, String> en = new HashMap<>();
    en.put("ban.kick", "You are banned by [actor] for [reason]");
    en.put("greeting", "Hello [player]");
    registry.loadLocale("en", en);

    Map<String, String> de = new HashMap<>();
    de.put("ban.kick", "Du wurdest von [actor] gebannt: [reason]");
    de.put("greeting", "Hallo [player]");
    registry.loadLocale("de", de);

    Message.init(registry, new TestLogger());
  }

  @Test
  public void resolveWithDefaultLocale() {
    String result = Message.get("ban.kick")
        .set("actor", "Admin")
        .set("reason", "griefing")
        .resolve("en");

    assertEquals("You are banned by Admin for griefing", result);
  }

  @Test
  public void resolveWithSpecificLocale() {
    String result = Message.get("ban.kick")
        .set("actor", "Admin")
        .set("reason", "griefing")
        .resolve("de");

    assertEquals("Du wurdest von Admin gebannt: griefing", result);
  }

  @Test
  public void toStringUsesDefaultLocale() {
    String result = Message.get("greeting")
        .set("player", "Steve")
        .toString();

    assertEquals("Hello Steve", result);
  }

  @Test
  public void resolveWithPlayerLocale() {
    TestPlayer player = new TestPlayer(UUID.randomUUID(), "Steve", true, "de");
    String result = Message.get("greeting")
        .set("player", "Steve")
        .resolve(player.getLocale());

    assertEquals("Hallo Steve", result);
  }

  @Test
  public void resolveForFallsBackWithoutPlugin() {
    TestPlayer player = new TestPlayer(UUID.randomUUID(), "Steve", true, "de");
    String result = Message.get("greeting")
        .set("player", "Steve")
        .resolveFor(player);

    assertEquals("Hello Steve", result);
  }

  @Test
  public void tokenReplacementOrderPreserved() {
    MessageRegistry registry = new MessageRegistry("en");
    Map<String, String> en = new HashMap<>();
    en.put("test.order", "[a] [b] [a]");
    registry.loadLocale("en", en);
    Message.init(registry, new TestLogger());

    String result = Message.get("test.order")
        .set("a", "X")
        .set("b", "Y")
        .toString();

    assertEquals("X Y X", result);
  }

  @Test
  public void missingKeyReturnsEmptyString() {
    String result = Message.get("nonexistent.key").toString();
    assertEquals("", result);
  }

  @Test
  public void dynamicRegistrationWritesToDefaultLocale() {
    new Message("custom.key", "Custom message");

    assertEquals("Custom message", Message.getString("custom.key"));
  }

  @Test
  public void replaceMethodWorks() {
    String result = Message.get("greeting")
        .set("player", "Steve")
        .replace("Hello", "Hey")
        .toString();

    assertEquals("Hey Steve", result);
  }
}
