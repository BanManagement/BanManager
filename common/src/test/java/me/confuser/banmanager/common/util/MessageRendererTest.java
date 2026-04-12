package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.kyori.text.Component;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class MessageRendererTest {

  private MessageRenderer renderer;

  @Before
  public void setUp() {
    renderer = MessageRenderer.getInstance();
  }

  @After
  public void tearDown() {
    renderer.loadStaticTokens(new HashMap<>());
  }

  @Test
  public void renderPlainText() {
    Component result = renderer.render("Hello world");
    String plain = renderer.toPlainText(result);
    assertEquals("Hello world", plain);
  }

  @Test
  public void renderWithPlaceholders() {
    Map<String, String> placeholders = new HashMap<>();
    placeholders.put("player", "Steve");
    Component result = renderer.render("<player> joined", placeholders);
    String plain = renderer.toPlainText(result);
    assertEquals("Steve joined", plain);
  }

  @Test
  public void renderMiniMessageFormatting() {
    Component result = renderer.render("<red>Error: <white>Something went wrong");
    String plain = renderer.toPlainText(result);
    assertEquals("Error: Something went wrong", plain);
  }

  @Test
  public void toLegacyPreservesColors() {
    Component result = renderer.render("<red>Red text");
    String legacy = renderer.toLegacy(result);
    assertTrue(legacy.contains("Red text"));
  }

  @Test
  public void toJsonProducesValidJson() {
    Component result = renderer.render("<green>Success");
    String json = renderer.toJson(result);
    assertTrue(json.startsWith("{") || json.startsWith("["));
    assertTrue(json.contains("Success"));
  }

  @Test
  public void isLegacyFormatDetectsAmpersandCodes() {
    assertTrue(renderer.isLegacyFormat("&cHello &eworld"));
    assertFalse(renderer.isLegacyFormat("<red>Hello <yellow>world"));
    assertFalse(renderer.isLegacyFormat("Plain text"));
  }

  @Test
  public void convertLegacyToMiniMessage() {
    String result = renderer.convertLegacyToMiniMessage("&cRed &lbold &agreen");
    assertFalse(result.contains("&c"));
    assertFalse(result.contains("&l"));
    assertFalse(result.contains("&a"));
  }

  @Test
  public void staticTokensAreResolved() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("server_name", "TestServer");
    renderer.loadStaticTokens(tokens);

    Component result = renderer.render("Welcome to <server_name>!");
    String plain = renderer.toPlainText(result);
    assertEquals("Welcome to TestServer!", plain);
  }

  @Test
  public void camelCaseTokensAreNormalised() {
    Map<String, String> tokens = new HashMap<>();
    tokens.put("serverName", "TestServer");
    renderer.loadStaticTokens(tokens);

    Component result = renderer.render("Welcome to <server_name>!");
    String plain = renderer.toPlainText(result);
    assertEquals("Welcome to TestServer!", plain);
  }

  @Test
  public void legacyConvertedExplicitlyThenRenders() {
    String mini = renderer.convertLegacyToMiniMessage("&cThis is legacy");
    Component result = renderer.render(mini);
    String plain = renderer.toPlainText(result);
    assertEquals("This is legacy", plain);
  }

  @Test
  public void escapeTagsPreventsInjection() {
    String malicious = "<red>injected</red>";
    String escaped = renderer.escapeTags(malicious);
    Component result = renderer.render("<gold>" + escaped);
    String plain = renderer.toPlainText(result);
    assertTrue("Escaped tags should render as literal text", plain.contains("<red>injected</red>"));
  }
}
