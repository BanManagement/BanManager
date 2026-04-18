package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.configuration.ConfigurationSection;
import me.confuser.banmanager.common.configuration.file.YamlConfiguration;
import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.minimessage.tag.resolver.Placeholder;
import me.confuser.banmanager.common.kyori.text.minimessage.tag.resolver.TagResolver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Validates that every message in the bundled messages_en.yml parses cleanly through MiniMessage.
 * Catches malformed tags, unknown tag usage, mismatched closing tags, and other translation bugs.
 */
public class MessagesYamlValidatorTest {

  private static final String MESSAGES_RESOURCE = "messages/messages_en.yml";

  private static final Set<String> KNOWN_DYNAMIC_TOKENS = new HashSet<>(Arrays.asList(
      "player", "name", "actor", "ip", "ip_address", "reason", "created", "expires", "remaining",
      "id", "uuid", "command", "comment", "message", "world", "x", "y", "z", "from", "to",
      "from_ip", "to_ip", "first_seen", "last_seen", "country", "city", "join", "leave",
      "type", "types", "rows", "amount", "size", "index", "page", "max_page", "count",
      "bans", "mutes", "kicks", "warns", "warn_points", "notes", "reports", "rangebans",
      "players", "file", "meta", "state", "hashtag", "appeal_url"
  ));

  private static final Set<String> KNOWN_STATIC_TOKENS = new HashSet<>(Arrays.asList(
      "appeal_url", "server_name", "discord_url", "rules_url"
  ));

  private MessageRenderer renderer;
  private List<String> errors;

  @BeforeEach
  public void setUp() {
    MessageRenderer.reset();
    renderer = MessageRenderer.getInstance();
    Map<String, String> staticTokens = new HashMap<>();
    for (String token : KNOWN_STATIC_TOKENS) {
      staticTokens.put(token, "test_" + token);
    }
    renderer.loadStaticTokens(staticTokens);
    errors = new ArrayList<>();
  }

  @AfterEach
  public void tearDown() {
    renderer.loadStaticTokens(new HashMap<>());
    MessageRenderer.reset();
  }

  @Test
  public void everyMessageParsesWithoutError() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    ConfigurationSection messages = conf.getConfigurationSection("messages");
    assertNotNull(messages, "messages section missing");

    TagResolver.Builder resolverBuilder = TagResolver.builder();
    for (String token : KNOWN_DYNAMIC_TOKENS) {
      resolverBuilder.resolver(Placeholder.unparsed(token, "test_" + token));
    }
    TagResolver dynamicResolver = resolverBuilder.build();

    int parsed = 0;
    for (String key : messages.getKeys(true)) {
      String value = messages.getString(key);
      if (value == null) continue;
      String processed = value.replace("\\n", "\n").replaceAll("(?<=\\n)(?=\\n)", " ");

      if (renderer.isLegacyFormat(processed)) {
        errors.add("messages." + key + ": legacy &-code formatting detected");
        continue;
      }

      try {
        Component component = renderer.render(processed, dynamicResolver);
        String plain = renderer.toPlainText(component);
        if (plain == null) {
          errors.add("messages." + key + ": render produced null plain text");
        }
        parsed++;
      } catch (Exception e) {
        errors.add("messages." + key + ": failed to render — " + e.getMessage()
            + " | template: " + processed);
      }
    }

    if (!errors.isEmpty()) {
      fail("Found " + errors.size() + " message issue(s):\n" + String.join("\n", errors));
    }
    assertTrue(parsed > 0, "Expected to parse at least 1 message");
  }

  @Test
  public void noUnknownTagsInMessages() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    ConfigurationSection messages = conf.getConfigurationSection("messages");

    Set<String> standardMiniMessageTags = new HashSet<>(Arrays.asList(
        // Color tags
        "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple",
        "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple",
        "yellow", "white", "color", "colour", "c",
        // Decoration tags (and shorthand)
        "bold", "b", "italic", "i", "em", "underlined", "u", "strikethrough", "st",
        "obfuscated", "obf",
        // Formatting tags
        "reset", "newline", "br", "rainbow", "gradient", "transition",
        // Interaction tags
        "click", "hover", "insertion", "selector", "sel",
        // Misc
        "key", "translatable", "tr", "lang", "score", "nbt", "font",
        // Pre-tag for verbatim
        "pre"
    ));

    Set<String> allowedTags = new HashSet<>(standardMiniMessageTags);
    allowedTags.addAll(KNOWN_DYNAMIC_TOKENS);
    allowedTags.addAll(KNOWN_STATIC_TOKENS);

    Pattern tagPattern = Pattern.compile("<([!/]?)([a-zA-Z_][a-zA-Z0-9_-]*)(?::[^>]*)?>");

    for (String key : messages.getKeys(true)) {
      String value = messages.getString(key);
      if (value == null) continue;

      Matcher m = tagPattern.matcher(value);
      while (m.find()) {
        String tagName = m.group(2).toLowerCase();
        if (allowedTags.contains(tagName)) continue;
        if (tagName.startsWith("#")) continue;

        errors.add("messages." + key + ": unknown tag <" + tagName + "> in: " + value);
      }
    }

    if (!errors.isEmpty()) {
      fail("Found " + errors.size() + " unknown tag(s):\n" + String.join("\n", errors));
    }
  }

  @Test
  public void noMismatchedClosingTags() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    ConfigurationSection messages = conf.getConfigurationSection("messages");

    Pattern tagPattern = Pattern.compile("<(/?)([a-zA-Z_][a-zA-Z0-9_-]*)(?::([^>]*))?>");

    for (String key : messages.getKeys(true)) {
      String value = messages.getString(key);
      if (value == null) continue;

      List<String> tagStack = new ArrayList<>();
      Matcher m = tagPattern.matcher(value);

      while (m.find()) {
        String slash = m.group(1);
        String tagName = m.group(2).toLowerCase();

        if (slash.isEmpty()) {
          tagStack.add(tagName);
        } else {
          if (tagStack.isEmpty()) {
            errors.add("messages." + key + ": closing tag </" + tagName
                + "> with no matching opening tag in: " + value);
          } else {
            tagStack.remove(tagStack.size() - 1);
          }
        }
      }
    }

    if (!errors.isEmpty()) {
      fail("Found " + errors.size() + " mismatched tag(s):\n" + String.join("\n", errors));
    }
  }

  @Test
  public void colorChoiceIsConsistentForActionButtons() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    ConfigurationSection messages = conf.getConfigurationSection("messages");

    String assign = messages.getString("report.actions.assign");
    String close = messages.getString("report.actions.close");
    String tp = messages.getString("report.actions.tp");

    assertNotNull(assign, "report.actions.assign missing");
    assertNotNull(close, "report.actions.close missing");
    assertNotNull(tp, "report.actions.tp missing");

    String buttonColor = "<dark_aqua>";
    assertTrue(assign.contains(buttonColor), "report.actions.assign should use " + buttonColor + " for visual consistency: " + assign);
    assertTrue(close.contains(buttonColor), "report.actions.close should use " + buttonColor + " for visual consistency: " + close);
    assertTrue(tp.contains(buttonColor), "report.actions.tp should use " + buttonColor + " for visual consistency: " + tp);
  }

  @Test
  public void noTypoMissingNoun_addnoteall() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    String value = conf.getString("messages.addnoteall.notify");
    assertNotNull(value);
    assertTrue(value.toLowerCase().contains("note"), "addnoteall.notify is missing the word 'note': " + value);
  }

  @Test
  public void noGrammarErrors_invalidReason() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    String value = conf.getString("messages.sender.error.invalidReason");
    assertNotNull(value);
    assertTrue(value.contains("is not a valid"), "invalidReason should say 'is not a valid reason' (was previously 'is no valid reason'): " + value);
  }

  @Test
  public void banPlayerKickAndDisallowedShareCriticalTokens() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    String disallowed = conf.getString("messages.ban.player.disallowed");
    String kick = conf.getString("messages.ban.player.kick");

    assertNotNull(disallowed, "ban.player.disallowed missing");
    assertNotNull(kick, "ban.player.kick missing");

    for (String token : new String[]{"<reason>", "<actor>", "<created>", "<appeal_url>"}) {
      assertTrue(disallowed.contains(token), "ban.player.disallowed should contain " + token + ": " + disallowed);
      assertTrue(kick.contains(token), "ban.player.kick should contain " + token + " for parity with disallowed: " + kick);
    }
  }

  @Test
  public void tempbanPlayerKickAndDisallowedShareCriticalTokens() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    String disallowed = conf.getString("messages.tempban.player.disallowed");
    String kick = conf.getString("messages.tempban.player.kick");

    assertNotNull(disallowed, "tempban.player.disallowed missing");
    assertNotNull(kick, "tempban.player.kick missing");

    for (String token : new String[]{"<reason>", "<actor>", "<created>", "<expires>", "<appeal_url>"}) {
      assertTrue(disallowed.contains(token), "tempban.player.disallowed should contain " + token + ": " + disallowed);
      assertTrue(kick.contains(token), "tempban.player.kick should contain " + token + " for parity with disallowed: " + kick);
    }
  }

  @Test
  public void dashboardHeaderRendersStaffDashboardText() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    String header = conf.getString("messages.dashboard.header");
    assertNotNull(header);

    Component component = renderer.render(header);
    String plain = renderer.toPlainText(component);
    assertTrue(plain.contains("Staff Dashboard"), "dashboard.header plain text should contain 'Staff Dashboard': " + plain);
  }

  @Test
  public void noLeftoverBracketTokens() throws Exception {
    YamlConfiguration conf = loadBundledMessages();
    ConfigurationSection messages = conf.getConfigurationSection("messages");

    Pattern bracketTokenPattern = Pattern.compile("\\[[a-zA-Z][a-zA-Z0-9_]*\\]");
    Set<String> allowedBracketLiterals = new HashSet<>(Arrays.asList(
        "[Info]", "[Unban]", "[Unmute]", "[Assign]", "[Close]", "[TP]", "[View]",
        "[Muted]", "[BanManager]"
    ));

    for (String key : messages.getKeys(true)) {
      String value = messages.getString(key);
      if (value == null) continue;

      Matcher m = bracketTokenPattern.matcher(value);
      while (m.find()) {
        String literal = m.group();
        if (allowedBracketLiterals.contains(literal)) continue;
        errors.add("messages." + key + ": suspected leftover bracket-style token "
            + literal + " in: " + value);
      }
    }

    if (!errors.isEmpty()) {
      fail("Found " + errors.size() + " leftover bracket-style token(s):\n" + String.join("\n", errors));
    }
  }

  private YamlConfiguration loadBundledMessages() throws Exception {
    InputStream in = getClass().getClassLoader().getResourceAsStream(MESSAGES_RESOURCE);
    assertNotNull(in, "Resource " + MESSAGES_RESOURCE + " not found on classpath");
    try (InputStream stream = in;
         Reader reader = new InputStreamReader(stream)) {
      return YamlConfiguration.loadConfiguration(reader);
    }
  }
}
