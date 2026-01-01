package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.kyori.text.serializer.legacy.LegacyComponentSerializer;
import me.confuser.banmanager.common.kyori.text.serializer.gson.GsonComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtils {
  // Pattern for &x&r&r&g&g&b&b format (Spigot-style hex)
  private static final Pattern SPIGOT_HEX_PATTERN = Pattern.compile(
      "&x(&[0-9a-fA-F])(&[0-9a-fA-F])(&[0-9a-fA-F])(&[0-9a-fA-F])(&[0-9a-fA-F])(&[0-9a-fA-F])"
  );

  // Parses &codes AND &#rrggbb format
  private static final LegacyComponentSerializer HEX_PARSER =
      LegacyComponentSerializer.builder()
          .character('&')
          .hexColors()
          .build();

  // Outputs legacy string with NO hex (downsampled to nearest vanilla)
  private static final LegacyComponentSerializer LEGACY_SERIALIZER =
      LegacyComponentSerializer.builder()
          .character('&')
          .build();

  // Outputs JSON with NO hex colors - safe for pre-1.16 clients
  private static final GsonComponentSerializer DOWNSAMPLING_JSON =
      GsonComponentSerializer.colorDownsamplingGson();

  /**
   * Convert Spigot-style &x&r&r&g&g&b&b to &#rrggbb format
   */
  private static String preprocessSpigotHex(String message) {
    Matcher matcher = SPIGOT_HEX_PATTERN.matcher(message);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      // Extract each color char (removing the & prefix)
      String hex = matcher.group(1).substring(1) +
                   matcher.group(2).substring(1) +
                   matcher.group(3).substring(1) +
                   matcher.group(4).substring(1) +
                   matcher.group(5).substring(1) +
                   matcher.group(6).substring(1);
      matcher.appendReplacement(result, "&#" + hex);
    }
    matcher.appendTail(result);
    return result.toString();
  }

  /**
   * Parse message with hex color support (&#rrggbb and &x&r&r&g&g&b&b)
   */
  public static Component parse(String message) {
    String processed = preprocessSpigotHex(message.replace("\\n", "\n"));
    return HEX_PARSER.deserialize(processed);
  }

  /**
   * Parse and convert to legacy string, downsampling hex to nearest vanilla.
   * Safe for ALL Minecraft versions (1.7.2+).
   */
  public static String toDownsampledLegacy(String message) {
    Component component = parse(message);
    return LEGACY_SERIALIZER.serialize(component);
  }

  /**
   * Parse and convert to JSON with downsampled colors.
   * Uses colorDownsamplingGson() - safe for pre-1.16 clients.
   */
  public static String toDownsampledJson(String message) {
    Component component = parse(message);
    return DOWNSAMPLING_JSON.serialize(component);
  }

  /**
   * Parse and convert to full JSON (with hex colors intact).
   * Only use when you KNOW the client supports 1.16+ colors.
   */
  public static String toJson(String message) {
    Component component = parse(message);
    return GsonComponentSerializer.gson().serialize(component);
  }
}
