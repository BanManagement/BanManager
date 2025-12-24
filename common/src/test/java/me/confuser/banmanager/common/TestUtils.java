package me.confuser.banmanager.common;

import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import me.confuser.banmanager.common.data.PlayerBanData;
import me.confuser.banmanager.common.data.PlayerData;
import me.confuser.banmanager.common.data.PlayerMuteData;

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

  public PlayerData createPlayerWithName(String name) {
    try {
      return plugin.getPlayerStorage().createIfNotExists(UUID.fromString(faker.internet().uuid()), name);
    } catch (SQLException e) {
      e.printStackTrace();
    }

    return null;
  }

  public String createRandomPlayerName() {
    String name = faker.name().username();

    return name.substring(0, Math.min(name.length(), 16));
  }

  public PlayerBanData createBan(PlayerData player, PlayerData actor, String reason) throws SQLException {
    PlayerBanData ban = new PlayerBanData(player, actor, reason, false);
    plugin.getPlayerBanStorage().ban(ban);
    return ban;
  }

  public void unbanPlayer(PlayerBanData ban, PlayerData actor) throws SQLException {
    plugin.getPlayerBanStorage().unban(ban, actor, "unbanned");
  }

  public PlayerMuteData createMute(PlayerData player, PlayerData actor, String reason) throws SQLException {
    PlayerMuteData mute = new PlayerMuteData(player, actor, reason, false, false);
    plugin.getPlayerMuteStorage().mute(mute);
    return mute;
  }

  public void unmutePlayer(PlayerMuteData mute, PlayerData actor) throws SQLException {
    plugin.getPlayerMuteStorage().unmute(mute, actor, "unmuted");
  }
}
