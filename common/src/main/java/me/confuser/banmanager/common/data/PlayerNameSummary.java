package me.confuser.banmanager.common.data;

/**
 * Summary of a player's usage of a particular name.
 * Aggregated from interval history: firstSeen = min(fromSeen), lastSeen = max(toSeen or current time).
 */
public record PlayerNameSummary(String name, long firstSeen, long lastSeen) {
}
