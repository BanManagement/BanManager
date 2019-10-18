package me.confuser.banmanager.common;

import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.util.Message;
import net.kyori.text.TextComponent;

import java.net.InetAddress;
import java.util.UUID;

public class TestPlayer implements CommonPlayer {

  private final UUID uuid;
  private final String name;
  private final boolean onlineMode;

  public TestPlayer(UUID uuid, String name, boolean onlineMode) {
    this.uuid = uuid;
    this.name = name;
    this.onlineMode = onlineMode;
  }

  @Override
  public void kick(String message) {
  }

  @Override
  public void sendMessage(String message) {
  }

  @Override
  public void sendMessage(Message message) {
  }

  @Override
  public void sendJSONMessage(TextComponent jsonString) {
  }

  @Override
  public boolean isConsole() {
    return false;
  }

  @Override
  public PlayerData getData() {
    return null;
  }

  @Override
  public boolean isOnlineMode() {
    return true;
  }

  @Override
  public boolean isOnline() {
    return false;
  }

  @Override
  public boolean hasPermission(String permission) {
    return true;
  }

  @Override
  public String getDisplayName() {
    return null;
  }

  @Override
  public String getName() {
    return null;
  }

  @Override
  public InetAddress getAddress() {
    return null;
  }

  @Override
  public UUID getUniqueId() {
    return null;
  }

  @Override
  public boolean teleport(CommonWorld world, double x, double y, double z, float pitch, float yaw) {
    return false;
  }
}
