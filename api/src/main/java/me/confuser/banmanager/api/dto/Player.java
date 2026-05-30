package me.confuser.banmanager.api.dto;

import inet.ipaddr.IPAddress;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable view of a known player.
 *
 * @param uuid the player's Mojang UUID
 * @param name the player's last-known name
 * @param ip the player's last-known IP address (may be {@code null} for the
 *           console actor or never-seen players)
 * @param lastSeen unix timestamp in seconds of the last login or activity
 * @param locale the player's selected locale, never {@code null} but possibly
 *               {@code Optional.empty()}
 */
public record Player(UUID uuid, String name, IPAddress ip, long lastSeen, Optional<String> locale) {

  public Player {
    Objects.requireNonNull(uuid, "uuid");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(locale, "locale");
  }

  public Player(UUID uuid, String name, IPAddress ip, long lastSeen) {
    this(uuid, name, ip, lastSeen, Optional.empty());
  }
}
