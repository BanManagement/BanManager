package me.confuser.banmanager.common;

public interface CommonMetrics {
  void submitOnlineMode(boolean online);
  void submitStorageType(String storageType);
  void submitStorageVersion(String version);
  void submitGlobalMode(boolean enabled);
  void submitGeoMode(boolean enabled);
  void submitDiscordMode(boolean enabled);
}
