package me.confuser.banmanager.bukkit;

import me.confuser.banmanager.common.kyori.text.Component;
import me.confuser.banmanager.common.util.MessageRenderer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Paper-only helper that uses native Adventure API for zero-overhead Component delivery.
 * Paper's Player implements Audience, so we cast to Audience for Adventure methods.
 * This class must only be loaded when Paper's Adventure API is on the classpath.
 */
class PaperAdventureHelper {

  private static final java.lang.reflect.Method KICK_METHOD;

  static {
    java.lang.reflect.Method m = null;
    try {
      m = Player.class.getMethod("kick", net.kyori.adventure.text.Component.class);
    } catch (NoSuchMethodException e) {
      // Expected on older Paper/Spigot versions without Component-based kick
    }
    KICK_METHOD = m;
  }

  private PaperAdventureHelper() {}

  static void sendMessage(Player player, Component component) {
    ((Audience) player).sendMessage(convertToNative(component));
  }

  static void sendActionBar(Player player, Component component) {
    ((Audience) player).sendActionBar(convertToNative(component));
  }

  static void showTitle(Player player, Component title, Component subtitle,
                        int fadeIn, int stay, int fadeOut) {
    net.kyori.adventure.text.Component nativeTitle = title != null
        ? convertToNative(title) : net.kyori.adventure.text.Component.empty();
    net.kyori.adventure.text.Component nativeSubtitle = subtitle != null
        ? convertToNative(subtitle) : net.kyori.adventure.text.Component.empty();

    Title.Times times = Title.Times.times(
        Duration.ofMillis(fadeIn * 50L),
        Duration.ofMillis(stay * 50L),
        Duration.ofMillis(fadeOut * 50L)
    );
    ((Audience) player).showTitle(Title.title(nativeTitle, nativeSubtitle, times));
  }

  static void kick(Player player, Component component) {
    if (KICK_METHOD != null) {
      try {
        KICK_METHOD.invoke(player, convertToNative(component));
        return;
      } catch (IllegalAccessException e) {
        java.util.logging.Logger.getLogger("BanManager").warning("Failed to invoke Paper kick method, falling back to legacy: " + e.getMessage());
      } catch (java.lang.reflect.InvocationTargetException e) {
        throw new IllegalStateException("Failed to kick player", e.getCause());
      }
    }
    player.kickPlayer(MessageRenderer.getInstance().toLegacy(component));
  }

  private static net.kyori.adventure.text.Component convertToNative(Component component) {
    String json = MessageRenderer.getInstance().toJson(component);
    return GsonComponentSerializer.gson().deserialize(json);
  }
}
