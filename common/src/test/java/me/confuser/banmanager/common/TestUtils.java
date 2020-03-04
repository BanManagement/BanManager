package me.confuser.banmanager.common;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import me.confuser.banmanager.common.data.PlayerData;

import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
public class TestUtils {
  private BanManagerPlugin plugin;
  private Faker faker;

  public PlayerData createRandomPlayer() {
    try {
      return plugin.getPlayerStorage().createIfNotExists(UUID.fromString(faker.internet().uuid()), createRandomPlayerName());
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public String createRandomPlayerName() {
    String name = faker.name().username();

    return name.substring(0, Math.min(name.length(), 16));
  }
}
