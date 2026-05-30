package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Active ban on a player. Use {@link me.confuser.banmanager.api.dto.PlayerBanRecord}
 * to inspect previous (expired or removed) bans.
 *
 * @param id storage row id
 * @param player the banned player
 * @param actor who issued the ban (may be the console)
 * @param reason ban reason
 * @param created unix timestamp seconds the ban was created
 * @param updated unix timestamp seconds the ban was last updated
 * @param expires unix timestamp seconds the ban expires; {@code 0} means
 *                permanent
 * @param silent whether the ban is silent (no broadcast)
 */
public record PlayerBan(
    int id,
    Player player,
    Player actor,
    String reason,
    long created,
    long updated,
    long expires,
    boolean silent
) {

  public PlayerBan {
    Objects.requireNonNull(player, "player");
    Objects.requireNonNull(actor, "actor");
    Objects.requireNonNull(reason, "reason");
  }

  public boolean isPermanent() {
    return expires == 0;
  }

  public boolean hasExpired() {
    return expires != 0 && expires <= (System.currentTimeMillis() / 1000L);
  }
}
