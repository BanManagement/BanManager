package me.confuser.banmanager.api.dto;

import java.util.Objects;

/**
 * Active ban on a player name.
 */
public record NameBan(
    int id,
    String name,
    Player actor,
    String reason,
    long created,
    long updated,
    long expires,
    boolean silent
) {

  public NameBan {
    Objects.requireNonNull(name, "name");
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
