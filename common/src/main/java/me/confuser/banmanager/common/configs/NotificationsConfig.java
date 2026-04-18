package me.confuser.banmanager.common.configs;

import lombok.Getter;
import me.confuser.banmanager.common.CommonLogger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class NotificationsConfig extends Config {

  @Getter
  private Map<String, NotificationChannel> staffChannels = new HashMap<>();
  @Getter
  private boolean mutedActionBar = true;
  @Getter
  private String warnedSound = "entity.villager.no";
  @Getter
  private float warnedSoundVolume = 1.0f;
  @Getter
  private float warnedSoundPitch = 1.0f;

  public NotificationsConfig(File dataFolder, CommonLogger logger) {
    super(dataFolder, "notifications.yml", logger);
  }

  @Override
  public void afterLoad() {
    staffChannels.clear();

    if (conf.getConfigurationSection("staff") != null) {
      for (String key : conf.getConfigurationSection("staff").getKeys(false)) {
        String path = "staff." + key;
        NotificationChannel channel = new NotificationChannel(
            conf.getBoolean(path + ".chat", true),
            conf.getBoolean(path + ".actionbar", false),
            conf.getBoolean(path + ".title", false),
            conf.getString(path + ".sound", ""),
            (float) conf.getDouble(path + ".soundVolume", 1.0),
            (float) conf.getDouble(path + ".soundPitch", 1.0),
            conf.getInt(path + ".titleFadeIn", 10),
            conf.getInt(path + ".titleStay", 70),
            conf.getInt(path + ".titleFadeOut", 20)
        );
        staffChannels.put(key, channel);
      }
    }

    if (conf.getConfigurationSection("player") != null) {
      if (conf.getConfigurationSection("player.muted") != null) {
        mutedActionBar = conf.getBoolean("player.muted.actionbar", true);
      }
      if (conf.getConfigurationSection("player.warned") != null) {
        warnedSound = conf.getString("player.warned.sound", "entity.villager.no");
        warnedSoundVolume = (float) conf.getDouble("player.warned.soundVolume", 1.0);
        warnedSoundPitch = (float) conf.getDouble("player.warned.soundPitch", 1.0);
      }
    }
  }

  @Override
  public void onSave() {
  }

  public NotificationChannel getStaffChannel(String event) {
    return staffChannels.getOrDefault(event, NotificationChannel.CHAT_ONLY);
  }

  @Getter
  public static class NotificationChannel {
    static final NotificationChannel CHAT_ONLY = new NotificationChannel(true, false, false, "", 1.0f, 1.0f, 10, 70, 20);

    private final boolean chat;
    private final boolean actionbar;
    private final boolean title;
    private final String sound;
    private final float soundVolume;
    private final float soundPitch;
    private final int titleFadeIn;
    private final int titleStay;
    private final int titleFadeOut;

    public NotificationChannel(boolean chat, boolean actionbar, boolean title,
                               String sound, float soundVolume, float soundPitch,
                               int titleFadeIn, int titleStay, int titleFadeOut) {
      this.chat = chat;
      this.actionbar = actionbar;
      this.title = title;
      this.sound = sound;
      this.soundVolume = soundVolume;
      this.soundPitch = soundPitch;
      this.titleFadeIn = titleFadeIn;
      this.titleStay = titleStay;
      this.titleFadeOut = titleFadeOut;
    }

    public boolean hasSound() {
      return sound != null && !sound.isEmpty();
    }
  }
}
