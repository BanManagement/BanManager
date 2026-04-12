package me.confuser.banmanager.common.util;

import lombok.Getter;
import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonLogger;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.PlaceholderResolver;
import me.confuser.banmanager.common.commands.CommonSender;
import me.confuser.banmanager.common.kyori.text.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Message {

  private static volatile MessageRegistry registry;
  private static volatile CommonLogger logger;
  // Matches PlaceholderAPI %placeholder% tokens. May also match non-PAPI patterns like %100%,
  // but the resolver's no-op return for unrecognised placeholders makes this harmless.
  private static final Pattern PAPI_PATTERN = Pattern.compile("%([^%]+)%");

  private static final String REPLACE_PREFIX = "__replace__";

  @Getter
  private String key;
  private final LinkedHashMap<String, String[]> replacements = new LinkedHashMap<>();

  public Message(String key) {
    this.key = key;

    if (registry != null && registry.getMessage(key) == null && logger != null) {
      logger.warning("Missing " + key + " message");
    }
  }

  public Message(String key, String message) {
    this.key = key;

    if (registry != null) {
      if (registry.getMessage(key) != null) {
        if (logger != null) logger.warning(key + " message already exists");
        return;
      }
      registry.putMessage(key, message);
    }
  }

  public static void init(MessageRegistry messageRegistry, CommonLogger commonLogger) {
    registry = messageRegistry;
    logger = commonLogger;
  }

  public static Message get(String key) {
    return new Message(key);
  }

  /**
   * Resolve a message template to a Component using the default locale.
   * Convenience for static messages with no dynamic tokens.
   */
  public static Component component(String key) {
    return get(key).resolveComponent();
  }

  /**
   * Resolve a message template to a Component using the specified locale.
   */
  public static Component component(String key, String locale) {
    return get(key).resolveComponent(locale);
  }

  public static String getString(String key) {
    if (registry == null) return null;
    String template = registry.getMessage(key);
    if (template == null) return null;
    MessageRenderer renderer = MessageRenderer.getInstance();
    return renderer.toLegacy(renderer.render(template));
  }

  public static String getString(String key, String locale) {
    if (registry == null) return null;
    String template = registry.getMessage(key, locale);
    if (template == null) return null;
    MessageRenderer renderer = MessageRenderer.getInstance();
    return renderer.toLegacy(renderer.render(template));
  }

  /**
   * Get the raw, unrendered template string for a message key.
   * Use this when you need to check for key existence or render
   * the template separately with dynamic context.
   */
  public static String getRawTemplate(String key) {
    if (registry == null) return null;
    return registry.getMessage(key);
  }

  /**
   * Get the raw, unrendered template string for this message's key.
   */
  public String getRawTemplate() {
    return getRawTemplate(this.key);
  }

  public Message replace(CharSequence oldChar, CharSequence newChar) {
    replacements.put(REPLACE_PREFIX + replacements.size(), new String[]{oldChar.toString(), newChar.toString()});
    return this;
  }

  public Message set(String token, String replace) {
    replacements.put(token, new String[]{token, replace});
    return this;
  }

  public Message set(String token, Integer replace) {
    return set(token, replace.toString());
  }

  public Message set(String token, Double replace) {
    return set(token, replace.toString());
  }

  public Message set(String token, Long replace) {
    return set(token, replace.toString());
  }

  public Message set(String token, Float replace) {
    return set(token, replace.toString());
  }

  /**
   * Resolve the message template to a legacy &-code string using the specified locale.
   */
  public String resolve(String locale) {
    Component component = resolveComponent(locale);
    return MessageRenderer.getInstance().toLegacy(component);
  }

  /**
   * Resolve the message template to a Component using the default locale.
   */
  public Component resolveComponent() {
    return resolveComponent(getDefaultLocale());
  }

  /**
   * Resolve the message template to a Component using MiniMessage.
   * Pipeline: raw template -> .replace() subs -> PAPI -> token replacement -> MiniMessage parse
   */
  public Component resolveComponent(String locale) {
    return resolveComponent(locale, null);
  }

  /**
   * Resolve the message template to a Component using MiniMessage, with optional PAPI resolution.
   */
  public Component resolveComponent(String locale, CommonPlayer player) {
    if (registry == null) return Component.empty();

    String template = registry.getMessage(key, locale);
    if (template == null) return Component.empty();

    MessageRenderer renderer = MessageRenderer.getInstance();

    // Step 1: Apply .replace() substitutions on the raw string
    template = applyRawReplacements(template);

    // Step 2: Resolve PAPI placeholders individually, escaping MiniMessage tags in output
    if (player != null && BanManagerPlugin.getInstance() != null) {
      PlaceholderResolver papiResolver = BanManagerPlugin.getInstance().getPlaceholderResolver();
      if (papiResolver != null) {
        template = resolvePapiSafe(papiResolver, player, template);
      }
    }

    // Step 3: Apply static and dynamic tokens as raw string replacements so they
    // resolve correctly inside click/hover arguments (MiniMessage TagResolvers
    // produce Components which can't be used as click event string values)
    template = applyTokenReplacements(template, renderer);

    // Step 4: Parse with MiniMessage (standard tags only, tokens already replaced)
    return renderer.render(template);
  }

  /**
   * Resolve the Component for a specific player, respecting per-player locale.
   */
  public Component resolveComponentFor(CommonPlayer player) {
    if (player == null) return resolveComponent(getDefaultLocale());

    BanManagerPlugin plugin = BanManagerPlugin.getInstance();
    String locale = getDefaultLocale();
    if (plugin != null && plugin.getConfig() != null && plugin.getConfig().isPerPlayerLocale()) {
      locale = player.getLocale();
    }
    return resolveComponent(locale, player);
  }

  public String resolveFor(CommonPlayer player) {
    Component component = resolveComponentFor(player);
    return MessageRenderer.getInstance().toLegacy(component);
  }

  public boolean sendTo(CommonSender sender) {
    if (sender == null) return false;

    Component component = resolveComponent(getDefaultLocale());
    sender.sendMessage(component);

    return true;
  }

  public boolean sendTo(CommonPlayer player) {
    if (player == null) return false;
    if (!player.isOnline()) return false;

    Component component = resolveComponentFor(player);
    player.sendMessage(component);

    return true;
  }

  public static boolean isJSONMessage(String message) {
    return message.startsWith("{") && message.endsWith("}") || message.startsWith("[") && message.endsWith("]");
  }

  @Override
  public String toString() {
    return resolve(getDefaultLocale());
  }

  /**
   * Apply .replace() raw substitutions on the template string before MiniMessage parsing.
   */
  private String applyRawReplacements(String template) {
    String result = template;
    for (Map.Entry<String, String[]> entry : replacements.entrySet()) {
      if (entry.getKey().startsWith(REPLACE_PREFIX)) {
        String[] pair = entry.getValue();
        result = result.replace(pair[0], pair[1]);
      }
    }
    return result;
  }

  /**
   * Resolve PAPI placeholders individually, escaping MiniMessage tags in each resolved value
   * to prevent injection of formatting/click/hover tags from external placeholder plugins.
   */
  private static String resolvePapiSafe(PlaceholderResolver resolver, CommonPlayer player, String template) {
    MessageRenderer renderer = MessageRenderer.getInstance();
    Matcher matcher = PAPI_PATTERN.matcher(template);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String placeholder = matcher.group(0);
      String resolved = resolver.resolve(player, placeholder);
      if (!resolved.equals(placeholder)) {
        matcher.appendReplacement(sb, Matcher.quoteReplacement(renderer.escapeTags(resolved)));
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  /**
   * Apply static and dynamic token replacements as raw string substitutions.
   * Values are tag-escaped to prevent MiniMessage injection.
   */
  private String applyTokenReplacements(String template, MessageRenderer renderer) {
    String result = template;

    for (Map.Entry<String, String> entry : renderer.getStaticTokens().entrySet()) {
      result = result.replace("<" + entry.getKey() + ">", renderer.escapeTags(entry.getValue()));
    }

    for (Map.Entry<String, String[]> entry : replacements.entrySet()) {
      if (!entry.getKey().startsWith(REPLACE_PREFIX)) {
        String tokenName = MessageRenderer.normaliseTagName(entry.getKey());
        String value = entry.getValue()[1];
        result = result.replace("<" + tokenName + ">", renderer.escapeTags(value));
      }
    }

    return result;
  }

  private static String getDefaultLocale() {
    if (registry == null) return "en";
    return registry.getDefaultLocale();
  }
}
