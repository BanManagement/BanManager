package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.PlayerMute;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class PlayerMutedEvent implements BanManagerEvent {

  private final PlayerMute mute;
  private final boolean silent;

  public PlayerMutedEvent(PlayerMute mute, boolean silent) {
    this.mute = Objects.requireNonNull(mute, "mute");
    this.silent = silent;
  }

  public PlayerMute mute() { return mute; }
  public boolean silent() { return silent; }
}
