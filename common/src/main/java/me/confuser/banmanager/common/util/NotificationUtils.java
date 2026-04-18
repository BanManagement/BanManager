package me.confuser.banmanager.common.util;

import me.confuser.banmanager.common.BanManagerPlugin;
import me.confuser.banmanager.common.CommonPlayer;
import me.confuser.banmanager.common.configs.NotificationsConfig;
import me.confuser.banmanager.common.kyori.text.Component;

public class NotificationUtils {

  private NotificationUtils() {}

  public static void notifyStaff(BanManagerPlugin plugin, String event, Message message, String permission) {
    if (!plugin.getConfig().isDisplayNotificationsEnabled()) return;

    NotificationsConfig.NotificationChannel channel = plugin.getNotificationsConfig().getStaffChannel(event);

    for (CommonPlayer player : plugin.getServer().getOnlinePlayers()) {
      if (!player.hasPermission(permission)) continue;

      Component resolved = message.resolveComponentFor(player);

      if (channel.isChat()) {
        player.sendMessage(resolved);
      }
      if (channel.isActionbar()) {
        player.sendActionBar(resolved);
      }
      if (channel.isTitle()) {
        player.showTitle(resolved, Component.empty(),
            channel.getTitleFadeIn(), channel.getTitleStay(), channel.getTitleFadeOut());
      }
      if (channel.hasSound()) {
        player.playSound(channel.getSound(), channel.getSoundVolume(), channel.getSoundPitch());
      }
    }

    plugin.getServer().getConsoleSender().sendMessage(
        MessageRenderer.getInstance().toPlainText(message.resolveComponent()));
  }

  public static void notifyPlayer(CommonPlayer player, Message message) {
    message.sendTo(player);
  }

  /**
   * Play the warned sound for a player without re-sending the message.
   */
  public static void playWarnedSound(BanManagerPlugin plugin, CommonPlayer player) {
    NotificationsConfig config = plugin.getNotificationsConfig();
    String sound = config.getWarnedSound();
    if (sound != null && !sound.isEmpty()) {
      player.playSound(sound, config.getWarnedSoundVolume(), config.getWarnedSoundPitch());
    }
  }
}
