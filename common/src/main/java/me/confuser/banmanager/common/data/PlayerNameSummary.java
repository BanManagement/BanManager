package me.confuser.banmanager.common.data;

import lombok.Getter;

/**
 * Summary of a player's usage of a particular name.
 * Aggregated from interval history: firstSeen = min(fromSeen), lastSeen = max(toSeen or current time).
 */
public class PlayerNameSummary {

  @Getter
  private final String name;

  @Getter
  private final long firstSeen;

  @Getter
  private final long lastSeen;

  public PlayerNameSummary(String name, long firstSeen, long lastSeen) {
    this.name = name;
    this.firstSeen = firstSeen;
    this.lastSeen = lastSeen;
  }
}
