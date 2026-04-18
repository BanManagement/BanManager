package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.TestLogger;
import me.confuser.banmanager.common.TestPlayer;
import me.confuser.banmanager.common.kyori.text.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class MessageDeferredTest {

  @BeforeEach
  public void setUp() {
    MessageRegistry registry = new MessageRegistry("en");

    Map<String, String> en = new HashMap<>();
    en.put("ban.kick", "You are banned by <actor> for <reason>");
    en.put("greeting", "Hello <player>");
    registry.loadLocale("en", en);

    Map<String, String> de = new HashMap<>();
    de.put("ban.kick", "Du wurdest von <actor> gebannt: <reason>");
    de.put("greeting", "Hallo <player>");
    registry.loadLocale("de", de);

    Message.init(registry, new TestLogger());
  }

  @Test
  public void resolveWithDefaultLocale() {
    Component component = Message.get("ban.kick")
        .set("actor", "Admin")
        .set("reason", "griefing")
        .resolveComponent("en");

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("You are banned by Admin for griefing", plain);
  }

  @Test
  public void resolveWithSpecificLocale() {
    Component component = Message.get("ban.kick")
        .set("actor", "Admin")
        .set("reason", "griefing")
        .resolveComponent("de");

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("Du wurdest von Admin gebannt: griefing", plain);
  }

  @Test
  public void toStringUsesDefaultLocale() {
    Component component = Message.get("greeting")
        .set("player", "Steve")
        .resolveComponent();

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("Hello Steve", plain);
  }

  @Test
  public void resolveWithPlayerLocale() {
    TestPlayer player = new TestPlayer(UUID.randomUUID(), "Steve", true, "de");
    Component component = Message.get("greeting")
        .set("player", "Steve")
        .resolveComponent(player.getLocale());

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("Hallo Steve", plain);
  }

  @Test
  public void resolveForFallsBackWithoutPlugin() {
    TestPlayer player = new TestPlayer(UUID.randomUUID(), "Steve", true, "de");
    Component component = Message.get("greeting")
        .set("player", "Steve")
        .resolveComponentFor(player);

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("Hello Steve", plain);
  }

  @Test
  public void tokenReplacementOrderPreserved() {
    MessageRegistry registry = new MessageRegistry("en");
    Map<String, String> en = new HashMap<>();
    en.put("test.order", "<first> <second> <first>");
    registry.loadLocale("en", en);
    Message.init(registry, new TestLogger());

    Component component = Message.get("test.order")
        .set("first", "X")
        .set("second", "Y")
        .resolveComponent();

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("X Y X", plain);
  }

  @Test
  public void missingKeyReturnsEmptyString() {
    String result = Message.get("nonexistent.key").toString();
    assertEquals("", result);
  }

  @Test
  public void dynamicRegistrationWritesToDefaultLocale() {
    new Message("custom.key", "Custom message");

    String result = Message.getString("custom.key");
    assertNotNull(result, "Dynamic registration should produce a non-null result");
    assertEquals("Custom message", result);
  }

  @Test
  public void replaceMethodWorks() {
    Component component = Message.get("greeting")
        .set("player", "Steve")
        .replace("Hello", "Hey")
        .resolveComponent();

    String plain = MessageRenderer.getInstance().toPlainText(component);
    assertEquals("Hey Steve", plain);
  }
}
