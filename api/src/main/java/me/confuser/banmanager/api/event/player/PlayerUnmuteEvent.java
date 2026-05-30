package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.event.AbstractCancellableEvent;

import java.util.Objects;

public final class PlayerUnmuteEvent extends AbstractCancellableEvent {

  private final PlayerMute mute;
  private final Player actor;
  private String reason;
  private boolean silent;

  public PlayerUnmuteEvent(PlayerMute mute, Player actor, String reason, boolean silent) {
    this.mute = Objects.requireNonNull(mute, "mute");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public PlayerMute mute() { return mute; }
  public Player actor() { return actor; }

  public String reason() { return reason; }
  public PlayerUnmuteEvent reason(String reason) { this.reason = reason; return this; }

  public boolean silent() { return silent; }
  public PlayerUnmuteEvent silent(boolean silent) { this.silent = silent; return this; }
}
