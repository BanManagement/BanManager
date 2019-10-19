package me.confuser.banmanager.common;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import me.confuser.banmanager.common.data.PlayerData;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.UUID;

@AllArgsConstructor
public class TestUtils {
  private BanManagerPlugin plugin;
  private Faker faker;

  public PlayerData createRandomPlayer() {
    try {
      return plugin.getPlayerStorage().createIfNotExists(UUID.fromString(faker.internet().uuid()), StringUtils.truncate(faker.name().username(), 16));
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }
}
