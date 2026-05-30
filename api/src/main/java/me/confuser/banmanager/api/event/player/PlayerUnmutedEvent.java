package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.Player;
import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class PlayerUnmutedEvent implements BanManagerEvent {

  private final PlayerMute mute;
  private final Player actor;
  private final String reason;
  private final boolean silent;

  public PlayerUnmutedEvent(PlayerMute mute, Player actor, String reason, boolean silent) {
    this.mute = Objects.requireNonNull(mute, "mute");
    this.actor = Objects.requireNonNull(actor, "actor");
    this.reason = Objects.requireNonNull(reason, "reason");
    this.silent = silent;
  }

  public PlayerMute mute() { return mute; }
  public Player actor() { return actor; }
  public String reason() { return reason; }
  public boolean silent() { return silent; }
}
