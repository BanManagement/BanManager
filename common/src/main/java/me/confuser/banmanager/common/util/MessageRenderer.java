package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.minimessage.MiniMessage;
import me.confuser.banmanager.common.kyori.text.minimessage.tag.Tag;
import me.confuser.banmanager.common.kyori.text.minimessage.tag.resolver.Placeholder;
import me.confuser.banmanager.common.kyori.text.minimessage.tag.resolver.TagResolver;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;
import me.confuser.banmanager.common.kyori.text.serializer.legacy.LegacyComponentSerializer;
import me.confuser.banmanager.common.kyori.text.serializer.plain.PlainTextComponentSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class MessageRenderer {

  private static MessageRenderer instance; // guarded by class-level synchronization

  private final MiniMessage miniMessage;
  private final LegacyComponentSerializer legacySerializer;
  private final PlainTextComponentSerializer plainTextSerializer;
  private volatile GsonComponentSerializer gsonSerializer;
  private volatile TagResolver staticTokenResolver = TagResolver.empty();
  private volatile Map<String, String> staticTokens = Collections.emptyMap();

  private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&[0-9a-fk-or]", Pattern.CASE_INSENSITIVE);
  private static final Pattern LEGACY_HEX_PATTERN = Pattern.compile("&#[0-9a-fA-F]{6}");

  private static final Map<Character, String> LEGACY_CODE_MAP = new HashMap<>();

  static {
    LEGACY_CODE_MAP.put('0', "<black>");
    LEGACY_CODE_MAP.put('1', "<dark_blue>");
    LEGACY_CODE_MAP.put('2', "<dark_green>");
    LEGACY_CODE_MAP.put('3', "<dark_aqua>");
    LEGACY_CODE_MAP.put('4', "<dark_red>");
    LEGACY_CODE_MAP.put('5', "<dark_purple>");
    LEGACY_CODE_MAP.put('6', "<gold>");
    LEGACY_CODE_MAP.put('7', "<gray>");
    LEGACY_CODE_MAP.put('8', "<dark_gray>");
    LEGACY_CODE_MAP.put('9', "<blue>");
    LEGACY_CODE_MAP.put('a', "<green>");
    LEGACY_CODE_MAP.put('b', "<aqua>");
    LEGACY_CODE_MAP.put('c', "<red>");
    LEGACY_CODE_MAP.put('d', "<light_purple>");
    LEGACY_CODE_MAP.put('e', "<yellow>");
    LEGACY_CODE_MAP.put('f', "<white>");
    LEGACY_CODE_MAP.put('k', "<obfuscated>");
    LEGACY_CODE_MAP.put('l', "<bold>");
    LEGACY_CODE_MAP.put('m', "<strikethrough>");
    LEGACY_CODE_MAP.put('n', "<underlined>");
    LEGACY_CODE_MAP.put('o', "<italic>");
    LEGACY_CODE_MAP.put('r', "<reset>");
  }

  public MessageRenderer() {
    this.miniMessage = MiniMessage.miniMessage();
    this.legacySerializer = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .build();
    this.plainTextSerializer = PlainTextComponentSerializer.plainText();
  }

  public static synchronized MessageRenderer getInstance() {
    if (instance == null) {
      instance = new MessageRenderer();
    }
    return instance;
  }

  public static synchronized void setInstance(MessageRenderer renderer) {
    instance = renderer;
  }

  /**
   * Reset the singleton instance. Intended for test teardown.
   */
  public static synchronized void reset() {
    instance = null;
  }

  /**
   * Render a MiniMessage template with additional tag resolvers.
   */
  public Component render(String template, TagResolver... extraResolvers) {
    if (template == null || template.isEmpty()) {
      return Component.empty();
    }

    TagResolver combined = buildResolver(extraResolvers);
    return miniMessage.deserialize(template, combined);
  }

  /**
   * Render a MiniMessage template with string placeholder replacements.
   * Values are inserted as literal text (safe from injection).
   */
  public Component render(String template, Map<String, String> placeholders) {
    if (placeholders == null || placeholders.isEmpty()) {
      return render(template);
    }

    List<TagResolver> resolvers = new ArrayList<>();
    for (Map.Entry<String, String> entry : placeholders.entrySet()) {
      resolvers.add(Placeholder.unparsed(entry.getKey(), entry.getValue()));
    }
    return render(template, TagResolver.resolver(resolvers));
  }

  /**
   * Serialize a Component to legacy &-code string.
   */
  public String toLegacy(Component component) {
    if (component == null) return "";
    return legacySerializer.serialize(component);
  }

  /**
   * Serialize a Component to JSON string.
   */
  public String toJson(Component component) {
    if (component == null) return "{}";
    return getGsonSerializer().serialize(component);
  }

  private GsonComponentSerializer getGsonSerializer() {
    if (gsonSerializer == null) {
      synchronized (this) {
        if (gsonSerializer == null) {
          gsonSerializer = GsonComponentSerializer.builder().build();
        }
      }
    }
    return gsonSerializer;
  }

  /**
   * Serialize a Component to plain text (no formatting).
   */
  public String toPlainText(Component component) {
    if (component == null) return "";
    return plainTextSerializer.serialize(component);
  }

  /**
   * Detect whether a string uses legacy &-code formatting.
   */
  public boolean isLegacyFormat(String template) {
    if (template == null) return false;
    return LEGACY_COLOR_PATTERN.matcher(template).find()
        || LEGACY_HEX_PATTERN.matcher(template).find();
  }

  /**
   * Convert a legacy &-code string to MiniMessage format.
   * Only converts color/formatting codes, not [token] placeholders.
   */
  public String convertLegacyToMiniMessage(String legacy) {
    if (legacy == null) return "";

    String result = legacy;

    // Convert &#rrggbb to <#rrggbb>
    result = result.replaceAll("&#([0-9a-fA-F]{6})", "<#$1>");

    // Convert &x&r&r&g&g&b&b (Spigot-style) to <#rrggbb>
    result = result.replaceAll(
        "&x&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])&([0-9a-fA-F])",
        "<#$1$2$3$4$5$6>"
    );

    // Convert &0-&f, &k-&o, &r to MiniMessage tags
    StringBuilder sb = new StringBuilder();
    int i = 0;
    while (i < result.length()) {
      if (result.charAt(i) == '&' && i + 1 < result.length()) {
        char code = Character.toLowerCase(result.charAt(i + 1));
        String replacement = LEGACY_CODE_MAP.get(code);
        if (replacement != null) {
          sb.append(replacement);
          i += 2;
          continue;
        }
      }
      sb.append(result.charAt(i));
      i++;
    }

    return sb.toString();
  }

  /**
   * Load user-defined static tokens from the messages file tokens section.
   */
  public void loadStaticTokens(Map<String, String> tokens) {
    if (tokens == null || tokens.isEmpty()) {
      this.staticTokens = Collections.emptyMap();
      this.staticTokenResolver = TagResolver.empty();
      return;
    }

    Map<String, String> normalised = new HashMap<>();
    List<TagResolver> resolvers = new ArrayList<>();
    for (Map.Entry<String, String> entry : tokens.entrySet()) {
      String key = normaliseTagName(entry.getKey());
      normalised.put(key, entry.getValue());
      resolvers.add(Placeholder.unparsed(key, entry.getValue()));
    }
    this.staticTokens = Collections.unmodifiableMap(normalised);
    this.staticTokenResolver = TagResolver.resolver(resolvers);
  }

  /**
   * Get the current static tokens map (for collision detection).
   */
  public Map<String, String> getStaticTokens() {
    return staticTokens;
  }

  /**
   * Build a combined TagResolver with proper priority:
   * 1. Dynamic tokens (from extraResolvers) -- highest priority
   * 2. Static user-defined tokens
   * 3. MiniMessage standard tags (colors, formatting, etc.)
   */
  private TagResolver buildResolver(TagResolver... extraResolvers) {
    if (extraResolvers == null || extraResolvers.length == 0) {
      return TagResolver.resolver(staticTokenResolver, TagResolver.standard());
    }

    TagResolver[] all = new TagResolver[extraResolvers.length + 2];
    System.arraycopy(extraResolvers, 0, all, 0, extraResolvers.length);
    all[extraResolvers.length] = staticTokenResolver;
    all[extraResolvers.length + 1] = TagResolver.standard();
    return TagResolver.resolver(all);
  }

  /**
   * Escape MiniMessage tags in a string so it renders as literal text.
   */
  public String escapeTags(String input) {
    if (input == null) return "";
    return miniMessage.escapeTags(input);
  }

  /**
   * Convert a relocated Component to a JSON string suitable for native Adventure deserialization.
   * Platform adapters can use this for JSON-based bridging to native Adventure.
   */
  public String toNativeJson(Component component) {
    return toJson(component);
  }

  /**
   * Normalise a tag name to match MiniMessage's [a-z0-9_-]* pattern.
   * Handles acronyms correctly: "playerIP" -> "player_ip", "serverURL" -> "server_url",
   * "firstName" -> "first_name", "HTMLParser" -> "html_parser".
   */
  public static String normaliseTagName(String name) {
    StringBuilder sb = new StringBuilder(name.length() + 4);
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (Character.isUpperCase(c)) {
        boolean prevUpper = i > 0 && Character.isUpperCase(name.charAt(i - 1));
        boolean nextLower = i + 1 < name.length() && Character.isLowerCase(name.charAt(i + 1));
        if (i > 0 && (!prevUpper || nextLower)) {
          sb.append('_');
        }
        sb.append(Character.toLowerCase(c));
      } else if (c == ' ') {
        sb.append('_');
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

}
