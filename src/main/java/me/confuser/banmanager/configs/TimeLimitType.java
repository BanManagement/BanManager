package me.confuser.banmanager.configs;

import lombok.Getter;

public enum TimeLimitType {
  PLAYER_BAN("playerBans"), PLAYER_MUTE("playerMutes"), IP_BAN("ipBans"), IP_MUTE("ipMutes"), PLAYER_WARN("playerWarnings");

  @Getter
  private final String name;

  TimeLimitType(String name) {
    this.name = name;
  }
}
