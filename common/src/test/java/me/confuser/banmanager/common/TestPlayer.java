package me.confuser.banmanager.common;

import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.kyori.text.TextComponent;

import java.net.InetAddress;
import java.util.UUID;

public class TestPlayer implements CommonPlayer {

  private final UUID uuid;
  private final String name;
  private final boolean onlineMode;
  private String locale = "en";

  public TestPlayer(UUID uuid, String name, boolean onlineMode) {
    this.uuid = uuid;
    this.name = name;
    this.onlineMode = onlineMode;
  }

  public TestPlayer(UUID uuid, String name, boolean onlineMode, String locale) {
    this(uuid, name, onlineMode);
    this.locale = locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  @Override
  public void kick(String message) {
  }

  @Override
  public void sendMessage(String message) {
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
  }

  @Override
  public void sendJSONMessage(String jsonString) {
  }

  @Override
  public boolean isConsole() {
    return false;
  }

  @Override
  public PlayerData getData() {
    return BanManagerPlugin.getInstance().getPlayerStorage().retrieve(this.name, false);
  }

  @Override
  public boolean isOnlineMode() {
    return this.onlineMode;
  }

  @Override
  public boolean isOnline() {
    return true;
  }

  @Override
  public boolean hasPermission(String permission) {
    return true;
  }

  @Override
  public String getDisplayName() {
    return this.name;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public InetAddress getAddress() {
    return null;
  }

  @Override
  public UUID getUniqueId() {
    return this.uuid;
  }

  @Override
  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }

  @Override
  public boolean canSee(CommonPlayer player) {
    return true;
  }

  @Override
  public String getLocale() {
    return locale;
  }
}
