package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Aggregated summary of a player's usage of a particular name.
 *
 * @param name the name
 * @param firstSeen earliest known {@code join} for this name (unix seconds)
 * @param lastSeen latest known {@code leave} (or current time for an active
 *                 session) for this name (unix seconds)
 */
public record PlayerNameSummary(String name, long firstSeen, long lastSeen) {

  public PlayerNameSummary {
    Objects.requireNonNull(name, "name");
  }
}
