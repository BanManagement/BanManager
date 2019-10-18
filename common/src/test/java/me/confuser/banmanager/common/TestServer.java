package me.confuser.banmanager.common;

import com.github.javafaker.Faker;
import me.confuser.banmanager.common.api.events.CommonEvent;
import me.confuser.banmanager.common.commands.CommonSender;
import net.kyori.text.TextComponent;

import java.util.UUID;

public class TestServer implements CommonServer {
  private Faker faker = new Faker();

  @Override
  public CommonPlayer getPlayer(UUID uniqueId) {
    return new TestPlayer(uniqueId, faker.name().username(), true);
  }

  @Override
  public CommonPlayer getPlayer(String name) {
    return new TestPlayer(UUID.fromString(faker.internet().uuid()), name, true);
  }

  @Override
  public CommonPlayer[] getOnlinePlayers() {
    return new CommonPlayer[0];
  }

  @Override
  public void broadcast(String message, String permission) {
  }

  @Override
  public void broadcastJSON(TextComponent message, String permission) {
  }

  @Override
  public void broadcast(String message, String permission, CommonSender sender) {
  }

  @Override
  public CommonSender getConsoleSender() {
    return null;
  }

  @Override
  public boolean dispatchCommand(CommonSender consoleSender, String command) {
    return true;
  }

  @Override
  public CommonWorld getWorld(String name) {
    return null;
  }

  @Override
  public CommonEvent callEvent(String name, Object... args) {
    return new CommonEvent(false, false);
  }
}
