package me.confuser.banmanager.api.event.player;

import me.confuser.banmanager.api.dto.PlayerWarn;
import me.confuser.banmanager.api.event.BanManagerEvent;

import java.util.Objects;

public final class PlayerWarnedEvent implements BanManagerEvent {

  private final PlayerWarn warn;
  private final boolean silent;

  public PlayerWarnedEvent(PlayerWarn warn, boolean silent) {
    this.warn = Objects.requireNonNull(warn, "warn");
    this.silent = silent;
  }

  public PlayerWarn warn() { return warn; }
  public boolean silent() { return silent; }
}
