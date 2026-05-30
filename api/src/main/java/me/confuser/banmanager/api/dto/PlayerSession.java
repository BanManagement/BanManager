package me.confuser.banmanager.api.dto;

import inet.ipaddr.IPAddress;

import java.util.Objects;
import java.util.Optional;

/**
 * Single login/logout session entry from the player history table.
 *
 * @param id storage row id
 * @param player the player record (UUID + last-known name/ip)
 * @param name the name in use during this session
 * @param ip the IP in use during this session, when IP logging is enabled
 * @param join unix timestamp seconds when the session began
 * @param leave unix timestamp seconds when the session ended; {@code 0} for
 *              an open session
 */
public record PlayerSession(
    int id,
    Player player,
    String name,
    Optional<IPAddress> ip,
    long join,
    long leave
) {

  public PlayerSession {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(name, "name");
    Objects.requireNonNull(ip, "ip");
  }
}
