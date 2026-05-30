package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Active mute on a player.
 *
 * @param id storage row id
 * @param player the muted player
 * @param actor who issued the mute
 * @param reason mute reason
 * @param created unix timestamp seconds the mute was created
 * @param updated unix timestamp seconds the mute was last updated
 * @param expires unix timestamp seconds the mute expires; {@code 0} means
 *                permanent or {@link #onlineOnly() online-only}
 * @param soft whether the mute is soft (the player still sees their own
 *             messages)
 * @param silent whether the mute is silent (no broadcast)
 * @param onlineOnly whether the mute clock only ticks while the player is
 *                   online
 * @param pausedRemaining seconds remaining when the player last logged off,
 *                        if {@link #onlineOnly()} is {@code true}
 */
public record PlayerMute(
    int id,
    Player player,
    Player actor,
    String reason,
    long created,
    long updated,
    long expires,
    boolean soft,
    boolean silent,
    boolean onlineOnly,
    long pausedRemaining
) {

  public PlayerMute {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");
  }

  public boolean isPermanent() {
    return expires == 0 && !onlineOnly;
  }

  public boolean hasExpired() {
    return expires != 0 && expires <= (System.currentTimeMillis() / 1000L);
  }

  public boolean isPaused() {
    return onlineOnly && pausedRemaining > 0;
  }
}
